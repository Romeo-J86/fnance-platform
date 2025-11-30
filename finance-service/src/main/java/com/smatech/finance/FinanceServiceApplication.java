package com.smatech.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 13:49
 * projectName Finance Platform
 **/

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class FinanceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceServiceApplication.class, args);
    }
}
