package com.edunest.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.exceptions.NotFound;
import com.cloudinary.utils.ObjectUtils;
import com.edunest.error.CustomException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class CloudinaryConfiguration {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    private void init() {
        this.cloudinary = cloudinary();
    }

    private Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        try {
            Map<String, Object> uploadParams = new HashMap<>();
            if (StringUtils.hasText(folder)) {
                uploadParams.put("folder", folder);
            }

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(uploadParams));
            log.info("Successfully uploaded file to Cloudinary: publicId={}, folder={}", result.get("public_id"), folder);
            return result;
        } catch (Exception e) {
            log.error("Error uploading file to Cloudinary: folder={}, error={}", folder, e.getMessage(), e);
            throw new CustomException("cloudinary", "Failed to upload file to Cloudinary: " + e.getMessage());
        }
    }

    public Map<String, Object> getFile(String publicId) {
        try {
            return cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
        } catch (NotFound e) {
            log.info("Cloudinary file not found: publicId={}", publicId);
            throw new CustomException("cloudinary", "File not found: " + publicId);
        } catch (Exception e) {
            log.error("Error fetching file from Cloudinary: publicId={}, error={}", publicId, e.getMessage(), e);
            throw new CustomException("cloudinary", "Failed to fetch file from Cloudinary: " + e.getMessage());
        }
    }

    public Map<String, Object> updateFile(MultipartFile file, String publicId) {
        try {
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("public_id", publicId);
            uploadParams.put("overwrite", true);

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(uploadParams));
            log.info("Successfully updated Cloudinary file: publicId={}", publicId);
            return result;
        } catch (Exception e) {
            log.error("Error updating file on Cloudinary: publicId={}, error={}", publicId, e.getMessage(), e);
            throw new CustomException("cloudinary", "Failed to update file on Cloudinary: " + e.getMessage());
        }
    }

    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Successfully deleted Cloudinary file: publicId={}", publicId);
        } catch (Exception e) {
            log.error("Error deleting file from Cloudinary: publicId={}, error={}", publicId, e.getMessage(), e);
            throw new CustomException("cloudinary", "Failed to delete file from Cloudinary: " + e.getMessage());
        }
    }
}
