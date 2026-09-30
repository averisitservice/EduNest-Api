package com.edunest.util;

import com.edunest.error.CustomException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class FileHandler {
    public static File convertMultipartFileToFile(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(null, originalFilename != null ? originalFilename : "temp-file");
            multipartFile.transferTo(tempFile.toFile());
            return tempFile.toFile();
        } catch (IOException e) {
            throw new CustomException("FILE_CONVERSION_ERROR", "Error while converting to file");
        }
    }

    public static String generateUniqueS3Key(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "document";
        }
        return UUID.randomUUID().toString().substring(0, 6) + "_" + originalFilename;
    }
}
