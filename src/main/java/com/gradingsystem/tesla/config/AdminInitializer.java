package com.gradingsystem.tesla.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gradingsystem.tesla.model.User;
import com.gradingsystem.tesla.repository.UserRepository;

@Configuration
public class AdminInitializer {

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;
    
    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin already exists
            if (userRepository.findByEmail("admin@gradeai.com") == null) {
                User admin = User.builder()
                        .firstName("System")
                        .lastName("Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role("ADMIN")
                        .verified(true)
                        .approvedByAdmin(true)
                        .build();

                userRepository.save(admin);
            }
        };
    }
}
