package com.aimrp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * AI MRP 网关服务
 *
 * 功能：
 * - 请求路由
 * - 负载均衡
 * - 权限校验（可扩展）
 * - 日志记录
 * - 限流熔断 (Sentinel)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.aimrp.**.infrastructure.feign")
public class AimrpGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimrpGatewayApplication.class, args);
    }

    /**
     * 配置 Sentinel 限流的 RestTemplate
     */
    @Bean
    @SentinelRestTemplate
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
