package com.aimrp.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI MRP API 启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.aimrp"})
@MapperScan("com.aimrp.**.infrastructure.persistence.mapper")
@EnableFeignClients(basePackages = {"com.aimrp.**.infrastructure.feign"})
public class AimrpApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimrpApiApplication.class, args);
    }
}
