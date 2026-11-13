package com.rit.gpa.gpa;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class GradePointConfig {
    private final Map<String, Double> gradePoints = new HashMap<>();

    public GradePointConfig() {
        initDefaultScale();
    }

    private void initDefaultScale() {
        // Official RIT Provisional Grade Scale
        // O = 10, A+ = 9, A = 8, B+ = 7, B = 6, C = 5, U = 0
        gradePoints.put("O", 10.0);
        gradePoints.put("A+", 9.0);
        gradePoints.put("A", 8.0);
        gradePoints.put("B+", 7.0);
        gradePoints.put("B", 6.0);
        gradePoints.put("C", 5.0);
        gradePoints.put("U", 0.0);
        gradePoints.put("RA", 0.0);
        gradePoints.put("SA", 0.0);
        gradePoints.put("W", 0.0);
        gradePoints.put("AB", 0.0);
    }

    public Map<String, Double> getGradePoints() {
        return Collections.unmodifiableMap(gradePoints);
    }

    public void setGradePoints(Map<String, Double> customPoints) {
        if (customPoints != null && !customPoints.isEmpty()) {
            for (Map.Entry<String, Double> entry : customPoints.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    String cleanKey = normalizeGradeKey(entry.getKey());
                    this.gradePoints.put(cleanKey, entry.getValue());
                }
            }
        }
    }

    /**
     * Resolves exact grade point.
     * Guarantees strict distinction: A+ = 9.0 and A = 8.0.
     */
    public Double getGradePoint(String grade) {
        if (grade == null) return null;
        String clean = normalizeGradeKey(grade);

        // Strict explicit mapping guarantees A+ is NEVER confused with A
        switch (clean) {
            case "O":
                return 10.0;
            case "A+":
                return 9.0;
            case "A":
                return 8.0;
            case "B+":
                return 7.0;
            case "B":
                return 6.0;
            case "C":
                return 5.0;
            case "U":
            case "RA":
            case "SA":
            case "W":
            case "AB":
            case "FAIL":
                return 0.0;
            default:
                return gradePoints.get(clean);
        }
    }

    public boolean isValidGrade(String grade) {
        if (grade == null) return false;
        String clean = normalizeGradeKey(grade);
        return "O".equals(clean) || "A+".equals(clean) || "A".equals(clean) ||
               "B+".equals(clean) || "B".equals(clean) || "C".equals(clean) ||
               "U".equals(clean) || "RA".equals(clean) || "SA".equals(clean) ||
               "W".equals(clean) || "AB".equals(clean) || gradePoints.containsKey(clean);
    }

    public boolean isArrear(String grade) {
        if (grade == null) return false;
        String g = normalizeGradeKey(grade);
        return "U".equals(g) || "RA".equals(g) || "FAIL".equals(g) || "AB".equals(g);
    }

    private String normalizeGradeKey(String key) {
        String trimmed = key.trim().toUpperCase();
        // Handle bracketed or spelled forms from YAML/Spring
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if ("A_PLUS".equals(trimmed) || "APLUS".equals(trimmed)) {
            return "A+";
        }
        if ("B_PLUS".equals(trimmed) || "BPLUS".equals(trimmed)) {
            return "B+";
        }
        return trimmed;
    }
}
