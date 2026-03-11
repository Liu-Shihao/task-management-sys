package com.finblock.tms;

import com.finblock.tms.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(AppProperties.class)
public class FinBlockTaskManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinBlockTaskManagementApplication.class, args);
    }
}

