package com.english.langchain4j;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.english.api.client")
@MapperScan("com.english.langchain4j.mapper")
@EnableDiscoveryClient
@SpringBootApplication
public class Langchain4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(Langchain4jApplication.class, args);
    }

}
