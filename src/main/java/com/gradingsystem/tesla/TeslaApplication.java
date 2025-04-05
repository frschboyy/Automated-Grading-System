package com.gradingsystem.tesla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:.env", ignoreResourceNotFound = true)
@SuppressWarnings("UseUtilityClass")
public class TeslaApplication {

    public static void main(final String[] args) {
        SpringApplication.run(TeslaApplication.class, args);
    }
}
