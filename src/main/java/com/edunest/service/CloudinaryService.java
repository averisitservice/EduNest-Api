package com.edunest.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {

    Map<String, Object> generateUploadSignature(String folder);

    Map<String, Object> uploadFile(MultipartFile file, String folder);

    Map<String, Object> getFile(String publicId);

    Map<String, Object> updateFile(MultipartFile file, String publicId);

    void deleteFile(String publicId);
}
