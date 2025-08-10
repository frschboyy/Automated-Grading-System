package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.service.FirebaseStorageService;

import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class DownloadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadController.class);

    private final FirebaseStorageService firebaseStorageService;

    @Autowired
    public DownloadController(FirebaseStorageService firebaseStorageService) {
        this.firebaseStorageService = firebaseStorageService;
    }

    @GetMapping("/download")
    public ResponseEntity<Void> downloadAssignment(@RequestParam("file") String filePath) {
        LOGGER.info("Received request to download file: {}", filePath);

        try {
            // Generate signed Firebase URL
            String signedUrl = firebaseStorageService.generateSignedUrl(filePath);

            LOGGER.info("Redirecting to signed URL: {}", signedUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(signedUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 redirect

        } catch (Exception e) {
            LOGGER.error("Failed to generate download link for file: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
