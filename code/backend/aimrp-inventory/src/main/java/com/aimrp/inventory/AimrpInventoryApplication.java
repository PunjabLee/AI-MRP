package com.aimrp.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI MRP 库存管理服务
 *
 * 支持两种运行模式：
 * 1. 单体模式 (standalone): 不注册到 Nacos
 * 2. 微服务模式 (microservice): 注册到 Nacos，支持服务发现
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.aimrp"})
@MapperScan("com.aimrp.**.infrastructure.persistence.mapper")
@EnableFeignClients(basePackages = {"com.aimrp.**.infrastructure.feign"})
@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", havingValue = "true", matchIfMissing = false)
@EnableDiscoveryClient
public class AimrpInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimrpInventoryApplication.class, args);
    }
}
