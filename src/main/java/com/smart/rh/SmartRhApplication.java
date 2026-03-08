package com.smart.rh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SmartRhApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartRhApplication.class, args);
    }
}
