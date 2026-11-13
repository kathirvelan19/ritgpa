package com.rit.gpa.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rit.gpa.dto.ExtractedResultResponse;
import com.rit.gpa.dto.ExtractedSubjectDto;
import com.rit.gpa.template.RitTemplateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfExtractionServiceTest {

    private PdfExtractionService pdfExtractionService;

    @BeforeEach
    void setUp() {
        RitTemplateManager templateManager = new RitTemplateManager(new DefaultResourceLoader(), new ObjectMapper());
        templateManager.init();
        pdfExtractionService = new PdfExtractionService(templateManager);
    }

    @Test
    @DisplayName("Extract reference RIT result PDF accurately")
    void testExtractReferencePdf() throws IOException {
        File sampleFile = new File("../sample-docs/Results (1).pdf");
        if (!sampleFile.exists()) {
            sampleFile = new File("sample-docs/Results (1).pdf");
        }
        assertTrue(sampleFile.exists(), "Sample PDF must exist at " + sampleFile.getAbsolutePath());

        byte[] bytes = Files.readAllBytes(sampleFile.toPath());
        ExtractedResultResponse response = pdfExtractionService.extractFromBytes(bytes);

        assertTrue(response.isSuccess(), "Extraction should succeed. Warnings: " + response.getWarnings());
        assertEquals("R KATHIRVELAN", response.getStudentName());
        assertEquals("2117240060037", response.getRegisterNumber());
        assertEquals("2025-2026", response.getAcademicYear());
        assertEquals("04", response.getSemester());
        assertEquals("B.E. MECH", response.getCourse());

        List<ExtractedSubjectDto> subjects = response.getSubjects();
        assertEquals(9, subjects.size(), "Should extract 9 subjects");

        // Verify subject 1
        assertEquals("GE23412", subjects.get(0).getSubjectCode());
        assertEquals("Environmental Science for Mechanical Engineers", subjects.get(0).getSubjectName());
        assertEquals("O", subjects.get(0).getGrade());
        assertEquals("PASS", subjects.get(0).getResultStatus());

        // Verify subject 3
        assertEquals("ME23412", subjects.get(2).getSubjectCode());
        assertEquals("Manufacturing Technology", subjects.get(2).getSubjectName());
        assertEquals("A+", subjects.get(2).getGrade());

        // Verify multi-line subject 8
        assertEquals("ME23422", subjects.get(7).getSubjectCode());
        assertEquals("Strength of Materials and Fluid Machinery Laboratory", subjects.get(7).getSubjectName());
        assertEquals("O", subjects.get(7).getGrade());

        // Verify subject 9
        assertEquals("ME23IC1", subjects.get(8).getSubjectCode());
        assertEquals("Introduction to Product Lifecycle Management", subjects.get(8).getSubjectName());
        assertEquals("O", subjects.get(8).getGrade());
    }

    @Test
    @DisplayName("Reject non-RIT or corrupted document")
    void testRejectInvalidDocument() {
        byte[] dummyBytes = "This is not a valid RIT result PDF document.".getBytes();
        ExtractedResultResponse response = assertDoesNotThrow(() ->
                pdfExtractionService.extractFromBytes(dummyBytes));
        assertFalse(response.isSuccess());
    }
}
