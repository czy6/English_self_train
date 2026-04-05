package com.english.user.controller;

import com.english.user.domain.vo.Result;
import com.english.user.utils.AliyunOSSOperator;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {
    
    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    @ApiOperation("上传图片接口")
    @PostMapping
    public Result uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        log.info("上传的file名称: {}", file.getOriginalFilename());
        // 将文件交给阿里云OSS
        String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
        log.info("文件url: {}", url);
        return Result.ok(url);
    }
    
}
