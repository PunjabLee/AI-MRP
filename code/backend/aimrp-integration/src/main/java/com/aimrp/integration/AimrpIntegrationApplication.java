package com.aimrp.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI MRP 集成服务 (ERP对接)
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.aimrp"})
@EnableFeignClients(basePackages = {"com.aimrp.**.infrastructure.feign"})
@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", havingValue = "true", matchIfMissing = false)
@EnableDiscoveryClient
public class AimrpIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimrpIntegrationApplication.class, args);
    }
}
