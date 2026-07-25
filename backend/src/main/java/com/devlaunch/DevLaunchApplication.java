package com.devlaunch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * DevLaunch Backend – AI-Powered Developer Career Hub.
 * <p>
 * Entry point for the Spring Boot application.
 * </p>
 */
@SpringBootApplication
@EnableJpaAuditing
public class DevLaunchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevLaunchApplication.class, args);
    }

}
