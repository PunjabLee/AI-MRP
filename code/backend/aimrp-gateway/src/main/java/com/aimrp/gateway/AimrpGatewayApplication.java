package com.aimrp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI MRP 网关服务
 *
 * 功能：
 * - 请求路由
 * - 负载均衡
 * - 权限校验（可扩展）
 * - 日志记录
 * - 限流熔断（可扩展）
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.aimrp.**.infrastructure.feign")
public class AimrpGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimrpGatewayApplication.class, args);
    }
}
