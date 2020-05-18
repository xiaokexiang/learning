package com.siemens.websocket.basicone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableEurekaClient
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@RestController
public class BasicOneApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicOneApplication.class, args);
    }

    @RequestMapping("index")
    public String index() {
        return "hello";
    }
}

