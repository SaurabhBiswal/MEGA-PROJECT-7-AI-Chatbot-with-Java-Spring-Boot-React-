package com.labmentix.aichatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${storage.provider:supabase}")
    private String storageProvider;

    // Supabase Config
    @Value("${supabase.url:}")
    private String supabaseUrl;
    @Value("${supabase.key:}")
    private String supabaseKey;
    @Value("${supabase.bucket:chat-attachments}")
    private String supabaseBucket;

    // AWS Config
    @Value("${aws.s3.access-key:}")
    private String awsAccessKey;
    @Value("${aws.s3.secret-key:}")
    private String awsSecretKey;
    @Value("${aws.s3.region:us-east-1}")
    private String awsRegion;
    @Value("${aws.s3.bucket:}")
    private String awsBucket;

    private S3Client s3Client;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PostConstruct
    public void init() {
        if ("s3".equalsIgnoreCase(storageProvider) && !awsAccessKey.isEmpty()) {
            this.s3Client = S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                    .build();
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // Sanitize the original filename to remove spaces and special characters
        String sanitizedFilename = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = UUID.randomUUID().toString() + "_" + sanitizedFilename;

        if ("s3".equalsIgnoreCase(storageProvider) && s3Client != null) {
            return uploadToS3(file, fileName);
        } else {
            return uploadToSupabase(file, fileName);
        }
    }

    private String uploadToS3(MultipartFile file, String fileName) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsBucket)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        return String.format("https://%s.s3.%s.amazonaws.com/%s", awsBucket, awsRegion, fileName);
    }

    private String uploadToSupabase(MultipartFile file, String fileName) throws IOException {
        if (supabaseUrl == null || supabaseUrl.isEmpty() || supabaseKey == null || supabaseKey.isEmpty()) {
            throw new RuntimeException(
                    "Supabase Storage configuration is incomplete. Please set SUPABASE_URL and SUPABASE_KEY in Render environment variables.");
        }
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + fileName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + supabaseKey)
                .header("apikey", supabaseKey)
                .header("Content-Type", file.getContentType())
                .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return supabaseUrl + "/storage/v1/object/public/" + supabaseBucket + "/" + fileName;
            } else {
                throw new RuntimeException("Failed to upload file to Supabase: " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", e);
        }
    }
}
