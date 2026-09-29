package com.edunest.service;

import com.edunest.configuration.AwsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileStorageService {

    @Autowired
    AwsConfiguration awsConfiguration;

    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        return awsConfiguration.uploadFile(file, folder);
    }

    public Map<String, Object> getFile(String publicId) {
        return awsConfiguration.getFile(publicId);
    }

    public Map<String, Object> updateFile(MultipartFile file, String publicId) {
        return awsConfiguration.updateFile(file, publicId);
    }

    public void deleteFile(String publicId) {
        awsConfiguration.deleteFile(publicId);
    }
}
