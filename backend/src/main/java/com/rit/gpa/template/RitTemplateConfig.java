package com.rit.gpa.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RitTemplateConfig {
    private String templateId;
    private String name;
    private String version;
    private DocumentConfig document;
    private StudentInfoConfig studentInfo;
    private SubjectTableConfig subjectTable;
    private Map<String, Double> gradePoints = new HashMap<>();

    public static class DocumentConfig {
        private List<String> headerKeywords = new ArrayList<>();
        private double pageWidth;
        private double pageHeight;

        public List<String> getHeaderKeywords() {
            return headerKeywords;
        }

        public void setHeaderKeywords(List<String> headerKeywords) {
            this.headerKeywords = headerKeywords;
        }

        public double getPageWidth() {
            return pageWidth;
        }

        public void setPageWidth(double pageWidth) {
            this.pageWidth = pageWidth;
        }

        public double getPageHeight() {
            return pageHeight;
        }

        public void setPageHeight(double pageHeight) {
            this.pageHeight = pageHeight;
        }
    }

    public static class StudentInfoConfig {
        private Map<String, AnchorRule> anchors = new HashMap<>();

        public Map<String, AnchorRule> getAnchors() {
            return anchors;
        }

        public void setAnchors(Map<String, AnchorRule> anchors) {
            this.anchors = anchors;
        }
    }

    public static class AnchorRule {
        private String label;
        private String endAnchor;
        private String pattern;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getEndAnchor() {
            return endAnchor;
        }

        public void setEndAnchor(String endAnchor) {
            this.endAnchor = endAnchor;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }

    public static class SubjectTableConfig {
        private String startMarker;
        private String endMarker;
        private List<ColumnDef> columns = new ArrayList<>();

        public String getStartMarker() {
            return startMarker;
        }

        public void setStartMarker(String startMarker) {
            this.startMarker = startMarker;
        }

        public String getEndMarker() {
            return endMarker;
        }

        public void setEndMarker(String endMarker) {
            this.endMarker = endMarker;
        }

        public List<ColumnDef> getColumns() {
            return columns;
        }

        public void setColumns(List<ColumnDef> columns) {
            this.columns = columns;
        }
    }

    public static class ColumnDef {
        private String id;
        private String name;
        private double minX;
        private double maxX;
        private boolean required;
        private boolean multiline;
        private String validationRegex;
        private List<String> allowedValues;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getMinX() {
            return minX;
        }

        public void setMinX(double minX) {
            this.minX = minX;
        }

        public double getMaxX() {
            return maxX;
        }

        public void setMaxX(double maxX) {
            this.maxX = maxX;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public boolean isMultiline() {
            return multiline;
        }

        public void setMultiline(boolean multiline) {
            this.multiline = multiline;
        }

        public String getValidationRegex() {
            return validationRegex;
        }

        public void setValidationRegex(String validationRegex) {
            this.validationRegex = validationRegex;
        }

        public List<String> getAllowedValues() {
            return allowedValues;
        }

        public void setAllowedValues(List<String> allowedValues) {
            this.allowedValues = allowedValues;
        }
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DocumentConfig getDocument() {
        return document;
    }

    public void setDocument(DocumentConfig document) {
        this.document = document;
    }

    public StudentInfoConfig getStudentInfo() {
        return studentInfo;
    }

    public void setStudentInfo(StudentInfoConfig studentInfo) {
        this.studentInfo = studentInfo;
    }

    public SubjectTableConfig getSubjectTable() {
        return subjectTable;
    }

    public void setSubjectTable(SubjectTableConfig subjectTable) {
        this.subjectTable = subjectTable;
    }

    public Map<String, Double> getGradePoints() {
        return gradePoints;
    }

    public void setGradePoints(Map<String, Double> gradePoints) {
        this.gradePoints = gradePoints;
    }
}

