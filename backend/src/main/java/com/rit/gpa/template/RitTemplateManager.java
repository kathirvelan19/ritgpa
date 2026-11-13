package com.rit.gpa.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class RitTemplateManager {
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Value("${rit.gpa.template-path:templates/rit-result-template.json}")
    private String templatePath = "templates/rit-result-template.json";

    private RitTemplateConfig currentTemplate;

    public RitTemplateManager(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        loadTemplate();
    }

    public synchronized void loadTemplate() {
        if (templatePath == null || templatePath.isEmpty()) {
            templatePath = "templates/rit-result-template.json";
        }
        try {
            Resource resource = resourceLoader.getResource("classpath:" + templatePath);
            try (InputStream is = resource.getInputStream()) {
                this.currentTemplate = objectMapper.readValue(is, RitTemplateConfig.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RIT template configuration from " + templatePath, e);
        }
    }

    public RitTemplateConfig getCurrentTemplate() {
        return currentTemplate;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }
}
