package com.edunest.configuration;

import com.edunest.error.CustomException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.time.Duration;

@Slf4j
@Configuration
public class AwsConfiguration {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @PostConstruct
    private void init() {
        this.s3Client = s3Client();
        this.s3Presigner = s3Presigner();
    }

    private S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    private S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    public void uploadFile(String keyName, File file) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
            log.info("Successfully uploaded file to S3: bucket={}, key={}", bucketName, keyName);
        } catch (Exception e) {
            log.error("Error while uploading file: {}", e.getMessage(), e);
            throw new CustomException("aws", "Failed to upload file to S3: " + e.getMessage());
        }
    }

    public void updateFile(File file, String key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

            log.info("Successfully updated S3 file: bucket={}, key={}", bucketName, key);
        } catch (Exception e) {
            log.error("Error updating file on S3: key={}, error={}", key, e.getMessage(), e);
            throw new CustomException("aws", "Failed to update file on S3: " + e.getMessage());
        }
    }

    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted S3 file: bucket={}, key={}", bucketName, key);
        } catch (Exception e) {
            log.error("Error deleting file from S3: key={}, error={}", key, e.getMessage(), e);
            throw new CustomException("aws", "Failed to delete file from S3: " + e.getMessage());
        }
    }

    public String getPresignedUrl(String documentName) {
        try {
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(documentName)
                            .build())
                    .signatureDuration(Duration.ofHours(1))
                    .build();
            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
            return presignedGetObjectRequest.url().toString();
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage());
            return null;
        }
    }
}
