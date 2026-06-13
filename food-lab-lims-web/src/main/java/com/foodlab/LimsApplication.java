package com.foodlab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
public class LimsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LimsApplication.class, args);
        System.out.println("========================================");
        System.out.println("  食品检测LIMS系统启动成功!");
        System.out.println("  接口文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
