package com.siemens.websocket.basictwo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableEurekaClient
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class BasicTwoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicTwoApplication.class, args);
    }

}
