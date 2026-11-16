package com.rit.gpa.pdf;

import com.rit.gpa.dto.ExtractedResultResponse;
import com.rit.gpa.dto.ExtractedSubjectDto;
import com.rit.gpa.template.RitTemplateConfig;
import com.rit.gpa.template.RitTemplateManager;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfExtractionService {
    private static final Logger log = LoggerFactory.getLogger(PdfExtractionService.class);

    private static final Set<String> ANNOTATION_KEYWORDS = Set.of(
            "extract", "from here", "from this", "sub code", "sub namee", "which semester", "which year", "grade from", "result from"
    );

    private final RitTemplateManager templateManager;

    public PdfExtractionService(RitTemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public ExtractedResultResponse extractFromPdf(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or missing.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported.");
        }

        byte[] bytes = file.getBytes();
        return extractFromBytes(bytes);
    }

    public ExtractedResultResponse extractFromBytes(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("PDF file content is empty.");
        }

        RitTemplateConfig template = templateManager.getCurrentTemplate();
        if (template == null) {
            throw new IllegalStateException("RIT Template configuration is not initialized.");
        }

        ExtractedResultResponse response = new ExtractedResultResponse();

        try (PDDocument document = Loader.loadPDF(bytes)) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("PDF is password-protected or encrypted. Please upload an unprotected result PDF.");
            }

            int pageCount = document.getNumberOfPages();
            if (pageCount < 1) {
                throw new IllegalArgumentException("PDF contains no pages.");
            }

            // Extract plain text to validate document headers and student details
            PDFTextStripper textStripper = new PDFTextStripper();
            String fullDocumentText = textStripper.getText(document).replace('\u00a0', ' ');

            validateDocumentHeader(fullDocumentText, template);

            // Extract student info
            extractStudentDetails(fullDocumentText, template, response);

            // Extract subject table using coordinate-based extractor
            PdfCoordinateExtractor coordinateExtractor = new PdfCoordinateExtractor();
            coordinateExtractor.getText(document); // triggers writeString coordinate tracking

            extractSubjectTable(coordinateExtractor.getFragments(), template, response);

            response.setSuccess(true);
            response.setMessage("Successfully extracted " + response.getSubjects().size() + " subjects from RIT result PDF.");
        } catch (IllegalArgumentException e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.getWarnings().add(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to extract PDF: ", e);
            response.setSuccess(false);
            response.setMessage("Failed to parse PDF document: " + e.getMessage());
            response.getWarnings().add(e.getMessage());
        }

        return response;
    }

    private void validateDocumentHeader(String text, RitTemplateConfig template) {
        if (template.getDocument() != null && template.getDocument().getHeaderKeywords() != null) {
            for (String keyword : template.getDocument().getHeaderKeywords()) {
                if (!text.toLowerCase().contains(keyword.toLowerCase())) {
                    throw new IllegalArgumentException("Document does not match supported RIT result format. Missing keyword: '" + keyword + "'");
                }
            }
        }
    }

    private void extractStudentDetails(String text, RitTemplateConfig template, ExtractedResultResponse response) {
        if (template.getStudentInfo() == null || template.getStudentInfo().getAnchors() == null) {
            return;
        }

        // Student Name
        RitTemplateConfig.AnchorRule nameRule = template.getStudentInfo().getAnchors().get("studentName");
        if (nameRule != null) {
            Pattern p = Pattern.compile(nameRule.getPattern(), Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                response.setStudentName(cleanText(m.group(1)));
            }
        }

        // Academic Year
        RitTemplateConfig.AnchorRule yearRule = template.getStudentInfo().getAnchors().get("academicYear");
        if (yearRule != null) {
            Pattern p = Pattern.compile(yearRule.getPattern(), Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                response.setAcademicYear(cleanText(m.group(1)));
            }
        }

        // Register No
        RitTemplateConfig.AnchorRule regRule = template.getStudentInfo().getAnchors().get("registerNo");
        if (regRule != null) {
            Pattern p = Pattern.compile(regRule.getPattern(), Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                response.setRegisterNumber(cleanText(m.group(1)));
            }
        }

        // Semester
        RitTemplateConfig.AnchorRule semRule = template.getStudentInfo().getAnchors().get("semester");
        if (semRule != null) {
            Pattern p = Pattern.compile(semRule.getPattern(), Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                response.setSemester(cleanText(m.group(1)));
            }
        }

        // Course
        RitTemplateConfig.AnchorRule courseRule = template.getStudentInfo().getAnchors().get("course");
        if (courseRule != null) {
            Pattern p = Pattern.compile(courseRule.getPattern(), Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                String line = m.group(1);
                int newlineIdx = line.indexOf("\n");
                if (newlineIdx != -1) {
                    line = line.substring(0, newlineIdx);
                }
                response.setCourse(cleanText(line));
            }
        }
    }

    private void extractSubjectTable(List<TextFragment> fragments,
                                     RitTemplateConfig template,
                                     ExtractedResultResponse response) {
        RitTemplateConfig.SubjectTableConfig tableConfig = template.getSubjectTable();
        if (tableConfig == null || tableConfig.getColumns() == null) {
            return;
        }

        RitTemplateConfig.ColumnDef semCol = findCol(tableConfig, "subjectSemester");
        RitTemplateConfig.ColumnDef codeCol = findCol(tableConfig, "subjectCode");
        RitTemplateConfig.ColumnDef titleCol = findCol(tableConfig, "subjectTitle");
        RitTemplateConfig.ColumnDef gradeCol = findCol(tableConfig, "grade");
        RitTemplateConfig.ColumnDef resultCol = findCol(tableConfig, "result");
        RitTemplateConfig.ColumnDef examDateCol = findCol(tableConfig, "examDate");

        // 1. Dynamic table column detection from header row anchors (y around 275 - 300)
        double semMinX = semCol != null ? semCol.getMinX() : 30.0;
        double semMaxX = semCol != null ? semCol.getMaxX() : 115.0;
        double codeMinX = codeCol != null ? codeCol.getMinX() : 115.0;
        double codeMaxX = codeCol != null ? codeCol.getMaxX() : 195.0;
        double titleMinX = titleCol != null ? titleCol.getMinX() : 195.0;
        double titleMaxX = titleCol != null ? titleCol.getMaxX() : 395.0;
        double gradeMinX = gradeCol != null ? gradeCol.getMinX() : 395.0;
        double gradeMaxX = gradeCol != null ? gradeCol.getMaxX() : 440.0;
        double resultMinX = resultCol != null ? resultCol.getMinX() : 440.0;
        double resultMaxX = resultCol != null ? resultCol.getMaxX() : 492.0;
        double examDateMinX = examDateCol != null ? examDateCol.getMinX() : 492.0;
        double examDateMaxX = examDateCol != null ? examDateCol.getMaxX() : 570.0;

        // Check if header row contains Grade, Result, Exam Date to calibrate boundaries dynamically
        for (TextFragment tf : fragments) {
            if (tf.getY() >= 275.0 && tf.getY() <= 300.0) {
                String headerText = tf.getText().toLowerCase();
                if (headerText.equals("grade")) {
                    gradeMinX = tf.getX() - 2.0;
                    titleMaxX = gradeMinX;
                } else if (headerText.equals("result")) {
                    resultMinX = tf.getX() - 2.0;
                    gradeMaxX = resultMinX;
                } else if (headerText.startsWith("exam")) {
                    examDateMinX = tf.getX() - 2.0;
                    resultMaxX = examDateMinX;
                }
            }
        }

        Pattern codePattern = Pattern.compile(codeCol != null && codeCol.getValidationRegex() != null ?
                codeCol.getValidationRegex() : "^[A-Z]{2}[0-9A-Z]{3,6}$");

        // 2. Identify all subject codes below header line
        List<TextFragment> codeFragments = new ArrayList<>();
        for (TextFragment tf : fragments) {
            if (tf.getY() > 300.0 && tf.getX() >= codeMinX && tf.getX() <= codeMaxX) {
                if (codePattern.matcher(tf.getText()).matches() && !isAnnotation(tf.getText())) {
                    codeFragments.add(tf);
                }
            }
        }

        // Sort subject codes strictly from top to bottom
        codeFragments.sort(Comparator.comparingDouble(TextFragment::getY));

        List<ExtractedSubjectDto> subjects = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();

        for (int i = 0; i < codeFragments.size(); i++) {
            TextFragment codeFrag = codeFragments.get(i);
            float y = codeFrag.getY();

            // Compute vertical row bounding box
            float rowTop = (i > 0) ? (codeFragments.get(i - 1).getY() + y) / 2.0f : Math.max(302.0f, y - 18.0f);
            float rowBottom = (i < codeFragments.size() - 1) ? (y + codeFragments.get(i + 1).getY()) / 2.0f : y + 22.0f;

            List<TextFragment> titleFrags = new ArrayList<>();
            List<TextFragment> gradeFrags = new ArrayList<>();
            List<TextFragment> resFrags = new ArrayList<>();
            List<TextFragment> semFrags = new ArrayList<>();
            List<TextFragment> examDateFrags = new ArrayList<>();

            for (TextFragment tf : fragments) {
                if (tf.getY() >= rowTop && tf.getY() < rowBottom) {
                    if (isAnnotation(tf.getText())) {
                        continue;
                    }

                    float cx = tf.getX() + (tf.getWidth() / 2.0f);

                    if (cx >= titleMinX && cx < titleMaxX) {
                        titleFrags.add(tf);
                    } else if (cx >= gradeMinX && cx < gradeMaxX) {
                        gradeFrags.add(tf);
                    } else if (cx >= resultMinX && cx < resultMaxX) {
                        resFrags.add(tf);
                    } else if (cx >= semMinX && cx < semMaxX) {
                        semFrags.add(tf);
                    } else if (cx >= examDateMinX && cx <= examDateMaxX) {
                        examDateFrags.add(tf);
                    }
                }
            }

            // Combine multi-line subject title (sorting top to bottom, then left to right)
            titleFrags.sort(Comparator.comparingDouble(TextFragment::getY).thenComparingDouble(TextFragment::getX));
            String title = joinFragments(titleFrags);

            // Grade fragments
            gradeFrags.sort(Comparator.comparingDouble(TextFragment::getY).thenComparingDouble(TextFragment::getX));
            String grade = joinFragments(gradeFrags);

            // Result status
            resFrags.sort(Comparator.comparingDouble(TextFragment::getY).thenComparingDouble(TextFragment::getX));
            String result = joinFragments(resFrags);

            // Semester
            semFrags.sort(Comparator.comparingDouble(TextFragment::getY).thenComparingDouble(TextFragment::getX));
            String sem = joinFragments(semFrags);

            // Exam Date
            examDateFrags.sort(Comparator.comparingDouble(TextFragment::getY).thenComparingDouble(TextFragment::getX));
            String examDate = joinFragments(examDateFrags);

            ExtractedSubjectDto subject = new ExtractedSubjectDto();
            subject.setSubjectCode(codeFrag.getText());
            subject.setSubjectName(title);
            subject.setGrade(grade);
            subject.setResultStatus(result);
            subject.setSubjectSemester(!sem.isEmpty() ? sem : response.getSemester());
            subject.setExamDate(examDate);

            // Duplicate code detection
            if (seenCodes.contains(subject.getSubjectCode())) {
                subject.setUncertain(true);
                String dupMsg = "Duplicate subject code detected: " + subject.getSubjectCode();
                subject.setWarningMessage(dupMsg);
                response.getWarnings().add(dupMsg);
            } else {
                seenCodes.add(subject.getSubjectCode());
            }

            validateSubject(subject, gradeCol, response);
            subjects.add(subject);
        }

        response.setSubjects(subjects);

        if (subjects.isEmpty()) {
            response.getWarnings().add("No subjects could be extracted. Please ensure the uploaded PDF contains a standard RIT result table.");
        }
    }

    private boolean isAnnotation(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String lower = text.toLowerCase().trim();
        for (String kw : ANNOTATION_KEYWORDS) {
            if (lower.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private String joinFragments(List<TextFragment> list) {
        StringBuilder sb = new StringBuilder();
        for (TextFragment tf : list) {
            if (isAnnotation(tf.getText())) {
                continue;
            }
            if (sb.length() > 0) sb.append(" ");
            sb.append(tf.getText());
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    private void validateSubject(ExtractedSubjectDto subject, RitTemplateConfig.ColumnDef gradeCol, ExtractedResultResponse response) {
        if (subject.getSubjectCode() == null || subject.getSubjectCode().trim().isEmpty()) {
            subject.setUncertain(true);
            response.getWarnings().add("Encountered subject record with missing code.");
        }

        if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
            subject.setUncertain(true);
            response.getWarnings().add("Subject " + subject.getSubjectCode() + " has empty title.");
        }

        if (gradeCol != null && gradeCol.getAllowedValues() != null && !gradeCol.getAllowedValues().isEmpty()) {
            if (!gradeCol.getAllowedValues().contains(subject.getGrade())) {
                subject.setUncertain(true);
                String msg = "Subject " + subject.getSubjectCode() + " has unrecognized grade: '" + subject.getGrade() + "'";
                subject.setWarningMessage(msg);
                response.getWarnings().add(msg);
            }
        }
    }

    private RitTemplateConfig.ColumnDef findCol(RitTemplateConfig.SubjectTableConfig tableConfig, String id) {
        return tableConfig.getColumns().stream()
                .filter(c -> c.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    private String cleanText(String input) {
        if (input == null) return "";
        return input.replaceAll("[\\r\\n]+", " ").trim();
    }
}
