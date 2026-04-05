package com.english.user;

import com.english.user.utils.AliyunOSSOperator;
import com.english.user.utils.Mp3Uploader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class UserApplicationTests {

    @Autowired
    private Mp3Uploader mp3Uploader;

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    @Test
    void testUploadMp3() throws Exception {
        String filePath = "E:\\资料\\英语四级历年真题及答案解析1989-2021\\2013年6月CET4\\2013年6月第一套\\201306-CET4（第一套）.mp3";
        String url = mp3Uploader.uploadMp3FromPath(filePath, "CET4/2023-6(1)");
        System.out.println("url = " + url);
    }

}
