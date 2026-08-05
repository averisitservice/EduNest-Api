package com.edunest.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfiguration {

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    private boolean enabled = false;

    @PostConstruct
    private void init() {
        if (!StringUtils.hasText(credentialsPath)) {
            log.warn("firebase.credentials-path is not set; push notifications are disabled.");
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            enabled = true;
            log.info("Firebase Admin SDK initialized; push notifications are enabled.");
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK from {}: {}", credentialsPath, e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
