package com.aimrp.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI MRP API 启动类
 *
 * 支持两种运行模式：
 * 1. 单体模式 (standalone): 默认模式，不注册到 Nacos
 * 2. 微服务模式 (microservice): 注册到 Nacos，支持服务发现
 *
 * 切换方式：
 * - 单体模式: java -jar aimrp-api.jar
 * - 微服务模式: java -jar aimrp-api.jar --spring.profiles.active=microservice
 *             或设置环境变量 SPRING_PROFILES_ACTIVE=microservice
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.aimrp"})
@MapperScan("com.aimrp.**.infrastructure.persistence.mapper")
@EnableFeignClients(basePackages = {"com.aimrp.**.infrastructure.feign"})
@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", havingValue = "true", matchIfMissing = false)
@EnableDiscoveryClient
public class AimrpApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimrpApiApplication.class, args);
    }
}
