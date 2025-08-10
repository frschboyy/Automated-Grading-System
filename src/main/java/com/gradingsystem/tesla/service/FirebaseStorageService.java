package com.gradingsystem.tesla.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.firebase.cloud.StorageClient;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FirebaseStorageService {

    // Uploads File to Firebase Storage and returns the file path
    public void uploadFile(byte[] fileBytes, String fileName, String contentType) throws Exception {
        Bucket bucket = StorageClient.getInstance().bucket();
        bucket.create(fileName, fileBytes, contentType);
    }

    // Retrieves a temporary publicly accessible URL
    public URL getFileUrl(String filePath, int validDays) {
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(filePath);

        if (blob == null) {
            throw new IllegalArgumentException("File not found in Firebase Storage: " + filePath);
        }

        // Generate a signed URL
        URL signedUrl = blob.signUrl(validDays, TimeUnit.DAYS);
        return signedUrl;
    }

    // Deletes a file from Firebase Storage by its path
    public boolean deleteFile(String firebasePath) {
        try {
            Bucket bucket = StorageClient.getInstance().bucket();
            Blob blob = bucket.get(firebasePath);

            if (blob != null) {
                return blob.delete();
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting file: " + firebasePath, e);
            return false;
        }
    }

    // Download File
    public byte[] downloadFile(String filePath) {
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(filePath);

        if (blob == null) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        return blob.getContent();
    }

    public String generateSignedUrl(String filePath) {
        try {
            Bucket bucket = StorageClient.getInstance().bucket(); // get the bucket instance
            Blob blob = bucket.get(filePath);

            if (blob == null) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }

            URL signedUrl = blob.signUrl(1, TimeUnit.DAYS, Storage.SignUrlOption.withV4Signature());

            return signedUrl.toString();
        } catch (StorageException | IllegalArgumentException e) {
            throw new RuntimeException("Failed to generate signed URL for file: " + filePath, e);
        }
    }
}
