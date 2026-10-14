package com.orderhub.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    /** Injected instead of calling Instant.now() directly, so time-based logic is testable. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
