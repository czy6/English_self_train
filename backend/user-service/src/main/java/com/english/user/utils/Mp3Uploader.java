package com.english.user.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.english.common.utils.UserHolder;
import com.english.user.utils.AliyunOSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class Mp3Uploader {

    @Autowired
    private AliyunOSSProperties aliyunOSSProperties;

    public String uploadMp3FromPath(String filePath, String subDir) throws Exception {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();

        // 从环境变量中获取访问凭证
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // 获取当前系统日期的字符串,格式为 yyyy/MM
        String dir = "CET/Mp3";
        // 获取文件名
        String originalFilename = new File(filePath).getName();
        // 生成一个新的不重复的文件名
        String newFileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + subDir + "/" + newFileName;

        // 创建OSSClient实例
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 创建ObjectMetadata对象并设置Content-Type
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("audio/mpeg");

            // 创建PutObjectRequest对象
            PutObjectRequest request = new PutObjectRequest(bucketName, objectName, new FileInputStream(filePath));
            request.setMetadata(metadata);

            // 上传文件
            PutObjectResult result = ossClient.putObject(request);
            System.out.println("上传成功，ETag：" + result.getETag());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 返回访问URL
        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
    }
}