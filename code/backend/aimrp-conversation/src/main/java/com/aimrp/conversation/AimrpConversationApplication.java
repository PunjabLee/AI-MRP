package com.aimrp.conversation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI MRP 对话服务 (AI智能体编排)
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.aimrp"})
@MapperScan("com.aimrp.**.infrastructure.persistence.mapper")
@EnableFeignClients(basePackages = {"com.aimrp.**.infrastructure.feign"})
@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", havingValue = "true", matchIfMissing = false)
@EnableDiscoveryClient
public class AimrpConversationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimrpConversationApplication.class, args);
    }
}
