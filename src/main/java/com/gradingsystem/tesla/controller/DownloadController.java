package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.service.FirebaseStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'INSTITUTIONAL_ADMIN', 'ADMIN')")
public class DownloadController {

    private final FirebaseStorageService firebaseStorageService;

    @GetMapping("/download")
    public ResponseEntity<Void> downloadAssignment(@RequestParam("file") String filePath) {
        log.info("Received request to download file: {}", filePath);

        try {
            // Generate signed Firebase URL
            String signedUrl = firebaseStorageService.generateSignedUrl(filePath);

            log.info("Redirecting to signed URL: {}", signedUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(signedUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 redirect

        } catch (Exception e) {
            log.error("Failed to generate download link for file: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
