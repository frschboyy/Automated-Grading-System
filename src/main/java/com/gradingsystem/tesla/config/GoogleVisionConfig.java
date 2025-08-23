package com.gradingsystem.tesla.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.api.gax.core.FixedCredentialsProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;

@Configuration
public class GoogleVisionConfig {

    @Value("${google.cloud.credentials}")
    private String credentialsPath;

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws Exception {
        Resource resource = new ClassPathResource(credentialsPath.replace("classpath:", ""));
        try (InputStream fis = resource.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(fis)
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            return ImageAnnotatorClient.create(settings);
        }
    }
}
