package com.mas.impression;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PrintAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrintAgentApplication.class, args);
    }
}
