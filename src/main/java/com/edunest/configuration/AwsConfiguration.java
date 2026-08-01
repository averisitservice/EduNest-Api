package com.edunest.configuration;

import com.edunest.error.CustomException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @PostConstruct
    private void init() {
        this.s3Client = s3Client();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    private String buildKey(String folder, String originalFileName) {
        String uniqueName = UUID.randomUUID() + "_" + originalFileName;
        return StringUtils.hasText(folder) ? folder + "/" + uniqueName : uniqueName;
    }

    private String buildUrl(String key) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        try {
            String key = buildKey(folder, file.getOriginalFilename());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("Successfully uploaded file to S3: bucket={}, key={}", bucketName, key);

            Map<String, Object> response = new HashMap<>();
            response.put("public_id", key);
            response.put("secure_url", buildUrl(key));
            return response;
        } catch (Exception e) {
            log.error("Error uploading file to S3: folder={}, error={}", folder, e.getMessage(), e);
            throw new CustomException("aws", "Failed to upload file to S3: " + e.getMessage());
        }
    }

    public Map<String, Object> getFile(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("public_id", key);
            response.put("secure_url", buildUrl(key));
            response.put("bytes", headObjectResponse.contentLength());
            response.put("format", headObjectResponse.contentType());
            return response;
        } catch (NoSuchKeyException e) {
            log.info("S3 file not found: key={}", key);
            throw new CustomException("aws", "File not found: " + key);
        } catch (Exception e) {
            log.error("Error fetching file from S3: key={}, error={}", key, e.getMessage(), e);
            throw new CustomException("aws", "Failed to fetch file from S3: " + e.getMessage());
        }
    }

    public Map<String, Object> updateFile(MultipartFile file, String key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("Successfully updated S3 file: bucket={}, key={}", bucketName, key);

            Map<String, Object> response = new HashMap<>();
            response.put("public_id", key);
            response.put("secure_url", buildUrl(key));
            return response;
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
}
