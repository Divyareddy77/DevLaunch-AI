package com.devlaunch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DevLaunch Backend – AI-Powered Developer Career Hub.
 * <p>
 * Entry point for the Spring Boot application. Scheduling is enabled so
 * the job tracker can deliver daily reminders (e.g. "interview tomorrow")
 * through the existing notification module, and async execution is enabled
 * so transactional emails (e.g. password reset) are delivered off the
 * request thread.
 * </p>
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class DevLaunchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevLaunchApplication.class, args);
    }

}
