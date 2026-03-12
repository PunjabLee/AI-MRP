package com.aimrp.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI MRP 报表服务
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.aimrp"})
@MapperScan("com.aimrp.**.infrastructure.persistence.mapper")
@EnableFeignClients(basePackages = {"com.aimrp.**.infrastructure.feign"})
@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", havingValue = "true", matchIfMissing = false)
@EnableDiscoveryClient
public class AimrpReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimrpReportApplication.class, args);
    }
}
