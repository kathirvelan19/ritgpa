package com.rit.gpa.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.project-id:ritgpacalc}")
    private String projectId;

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    private boolean initialized = false;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            this.initialized = true;
            return;
        }

        try {
            FirebaseOptions.Builder builder = FirebaseOptions.builder()
                    .setProjectId(projectId);

            if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
                File credFile = new File(credentialsPath.trim());
                if (credFile.exists()) {
                    try (FileInputStream fis = new FileInputStream(credFile)) {
                        builder.setCredentials(GoogleCredentials.fromStream(fis));
                        log.info("Initialized Firebase using credentials file: {}", credentialsPath);
                    }
                } else {
                    log.warn("Configured Firebase credentials path '{}' does not exist. Falling back to default credentials.", credentialsPath);
                    builder.setCredentials(GoogleCredentials.getApplicationDefault());
                }
            } else {
                try {
                    builder.setCredentials(GoogleCredentials.getApplicationDefault());
                    log.info("Initialized Firebase using application default credentials for project {}", projectId);
                } catch (Exception e) {
                    log.warn("No Google application default credentials found. Running in local/test mode without Firebase Admin connection: {}", e.getMessage());
                    return;
                }
            }

            FirebaseApp.initializeApp(builder.build());
            this.initialized = true;
            log.info("Firebase App initialized successfully.");
        } catch (Exception e) {
            log.warn("Could not initialize Firebase App (local development mode active): {}", e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Bean
    public Firestore firestore() {
        if (initialized && !FirebaseApp.getApps().isEmpty()) {
            try {
                return FirestoreClient.getFirestore();
            } catch (Exception e) {
                log.warn("Firestore client could not be obtained: {}", e.getMessage());
            }
        }
        return null;
    }
}

