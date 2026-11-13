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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfExtractionService {
    private static final Logger log = LoggerFactory.getLogger(PdfExtractionService.class);

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

            // Extract plain text to validate document headers
            PDFTextStripper textStripper = new PDFTextStripper();
            String fullDocumentText = textStripper.getText(document).replace('\u00a0', ' ');

            validateDocumentHeader(fullDocumentText, template);

            // Extract student info
            extractStudentDetails(fullDocumentText, template, response);

            // Extract subject table using coordinate extractor
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
                // Strip following lines if any
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

        Pattern codePattern = Pattern.compile(codeCol != null && codeCol.getValidationRegex() != null ?
                codeCol.getValidationRegex() : "^[A-Z]{2}[0-9A-Z]{3,6}$");

        // 1. Locate subject code fragments
        List<TextFragment> codeFragments = new ArrayList<>();
        double codeMinX = codeCol != null ? codeCol.getMinX() : 120.0;
        double codeMaxX = codeCol != null ? codeCol.getMaxX() : 200.0;

        for (TextFragment tf : fragments) {
            if (tf.getY() > 260.0 && tf.getX() >= codeMinX && tf.getX() <= codeMaxX) {
                if (codePattern.matcher(tf.getText()).matches()) {
                    codeFragments.add(tf);
                }
            }
        }

        // Sort by y coordinate top to bottom
        codeFragments.sort(java.util.Comparator.comparingDouble(TextFragment::getY));

        List<ExtractedSubjectDto> subjects = new ArrayList<>();

        for (int i = 0; i < codeFragments.size(); i++) {
            TextFragment codeFrag = codeFragments.get(i);
            float y = codeFrag.getY();

            float rowTop = (i > 0) ? (codeFragments.get(i - 1).getY() + y) / 2.0f : 290.0f;
            float rowBottom = (i < codeFragments.size() - 1) ? (y + codeFragments.get(i + 1).getY()) / 2.0f : 750.0f;

            // Collect column fragments inside [rowTop, rowBottom]
            List<TextFragment> titleFrags = new ArrayList<>();
            List<TextFragment> gradeFrags = new ArrayList<>();
            List<TextFragment> resFrags = new ArrayList<>();
            List<TextFragment> semFrags = new ArrayList<>();
            List<TextFragment> examDateFrags = new ArrayList<>();

            for (TextFragment tf : fragments) {
                if (tf.getY() >= rowTop && tf.getY() < rowBottom) {
                    float cx = tf.getX() + (tf.getWidth() / 2.0f);

                    if (titleCol != null && cx >= titleCol.getMinX() && cx <= titleCol.getMaxX()) {
                        titleFrags.add(tf);
                    } else if (gradeCol != null && cx >= gradeCol.getMinX() && cx <= gradeCol.getMaxX()) {
                        gradeFrags.add(tf);
                    } else if (resultCol != null && cx >= resultCol.getMinX() && cx <= resultCol.getMaxX()) {
                        resFrags.add(tf);
                    } else if (semCol != null && cx >= semCol.getMinX() && cx <= semCol.getMaxX()) {
                        semFrags.add(tf);
                    } else if (examDateCol != null && cx >= examDateCol.getMinX() && cx <= examDateCol.getMaxX()) {
                        examDateFrags.add(tf);
                    }
                }
            }

            // Sort title and date fragments by Y then X
            titleFrags.sort(java.util.Comparator.comparingDouble(TextFragment::getY).thenComparingDouble(TextFragment::getX));
            examDateFrags.sort(java.util.Comparator.comparingDouble(TextFragment::getY).thenComparingDouble(TextFragment::getX));

            String title = joinFragments(titleFrags);
            String grade = joinFragments(gradeFrags);
            String result = joinFragments(resFrags);
            String sem = joinFragments(semFrags);
            String examDate = joinFragments(examDateFrags);

            ExtractedSubjectDto subject = new ExtractedSubjectDto();
            subject.setSubjectCode(codeFrag.getText());
            subject.setSubjectName(title);
            subject.setGrade(grade);
            subject.setResultStatus(result);
            subject.setSubjectSemester(!sem.isEmpty() ? sem : response.getSemester());
            subject.setExamDate(examDate);

            validateSubject(subject, gradeCol, response);
            subjects.add(subject);
        }

        response.setSubjects(subjects);

        if (subjects.isEmpty()) {
            response.getWarnings().add("No subjects could be extracted. Please ensure the uploaded PDF contains a standard RIT result table.");
        }
    }

    private String joinFragments(List<TextFragment> list) {
        StringBuilder sb = new StringBuilder();
        for (TextFragment tf : list) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(tf.getText());
        }
        return sb.toString().trim();
    }

    private void validateSubject(ExtractedSubjectDto subject, RitTemplateConfig.ColumnDef gradeCol, ExtractedResultResponse response) {
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
