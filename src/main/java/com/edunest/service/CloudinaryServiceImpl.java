package com.edunest.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.edunest.error.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Map<String, Object> generateUploadSignature(String folder) {
        try {
            long timestamp = System.currentTimeMillis() / 1000;

            Map<String, Object> paramsToSign = new HashMap<>();
            paramsToSign.put("timestamp", timestamp);
            if (StringUtils.hasText(folder)) {
                paramsToSign.put("folder", folder);
            }

            String signature = cloudinary.apiSignRequest(paramsToSign, apiSecret);

            Map<String, Object> response = new HashMap<>();
            response.put("uploadUrl", "https://api.cloudinary.com/v1_1/" + cloudName + "/auto/upload");
            response.put("apiKey", apiKey);
            response.put("timestamp", timestamp);
            response.put("signature", signature);
            response.put("cloudName", cloudName);
            if (StringUtils.hasText(folder)) {
                response.put("folder", folder);
            }

            return response;
        } catch (Exception e) {
            throw new CustomException("cloudinary", "Failed to generate Cloudinary upload signature: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        try {
            Map<String, Object> uploadParams = new HashMap<>();
            if (StringUtils.hasText(folder)) {
                uploadParams.put("folder", folder);
            }

            return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(uploadParams));
        } catch (Exception e) {
            throw new CustomException("cloudinary", "Failed to upload file to Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getFile(String publicId) {
        try {
            return cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new CustomException("cloudinary", "Failed to fetch file from Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> updateFile(MultipartFile file, String publicId) {
        try {
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("public_id", publicId);
            uploadParams.put("overwrite", true);

            return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(uploadParams));
        } catch (Exception e) {
            throw new CustomException("cloudinary", "Failed to update file on Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new CustomException("cloudinary", "Failed to delete file from Cloudinary: " + e.getMessage());
        }
    }
}
