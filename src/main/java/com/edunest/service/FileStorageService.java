package com.edunest.service;

import com.edunest.configuration.AwsConfiguration;
import com.edunest.configuration.CloudinaryConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileStorageService {

    @Value("${is-live}")
    private boolean isLive;

    @Autowired
    AwsConfiguration awsConfiguration;

    @Autowired
    CloudinaryConfiguration cloudinaryConfiguration;

    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        return isLive
                ? awsConfiguration.uploadFile(file, folder)
                : cloudinaryConfiguration.uploadFile(file, folder);
    }

    public Map<String, Object> getFile(String publicId) {
        return isLive
                ? awsConfiguration.getFile(publicId)
                : cloudinaryConfiguration.getFile(publicId);
    }

    public Map<String, Object> updateFile(MultipartFile file, String publicId) {
        return isLive
                ? awsConfiguration.updateFile(file, publicId)
                : cloudinaryConfiguration.updateFile(file, publicId);
    }

    public void deleteFile(String publicId) {
        if (isLive) {
            awsConfiguration.deleteFile(publicId);
        } else {
            cloudinaryConfiguration.deleteFile(publicId);
        }
    }
}
