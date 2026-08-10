package com.edunest.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials-file:}")
    private String credentialsFile;

    private boolean enabled = false;

    @PostConstruct
    public void initialize() {
        if (credentialsFile == null || credentialsFile.isBlank()) {
            log.warn("firebase.credentials-file is not set; push notifications are disabled.");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            enabled = true;
            return;
        }

        try (InputStream serviceAccount = new ClassPathResource(credentialsFile).getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            enabled = true;
            log.info("Firebase Admin SDK initialized; push notifications are enabled.");
        } catch (Exception e) {
            log.error("Failed to initialize Firebase from '{}'; push notifications are disabled: {}",
                    credentialsFile, e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
