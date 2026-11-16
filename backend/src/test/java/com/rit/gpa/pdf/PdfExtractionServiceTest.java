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
    @DisplayName("Extract reference RIT result PDF accurately (Semester 04)")
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
        assertEquals(9, subjects.size(), "Should extract exactly 9 subjects");

        // Verify subject 1 (multi-line title)
        assertEquals("GE23412", subjects.get(0).getSubjectCode());
        assertEquals("Environmental Science for Mechanical Engineers", subjects.get(0).getSubjectName());
        assertEquals("O", subjects.get(0).getGrade());
        assertEquals("PASS", subjects.get(0).getResultStatus());
        assertEquals("04", subjects.get(0).getSubjectSemester());
        assertEquals("April 2026", subjects.get(0).getExamDate());

        // Verify subject 2
        assertEquals("ME23411", subjects.get(1).getSubjectCode());
        assertEquals("Engineering Materials and Metallurgy", subjects.get(1).getSubjectName());
        assertEquals("O", subjects.get(1).getGrade());

        // Verify subject 3
        assertEquals("ME23412", subjects.get(2).getSubjectCode());
        assertEquals("Manufacturing Technology", subjects.get(2).getSubjectName());
        assertEquals("A+", subjects.get(2).getGrade());

        // Verify subject 4
        assertEquals("ME23413", subjects.get(3).getSubjectCode());
        assertEquals("Mechanics of Solids", subjects.get(3).getSubjectName());
        assertEquals("O", subjects.get(3).getGrade());

        // Verify subject 5
        assertEquals("ME23414", subjects.get(4).getSubjectCode());
        assertEquals("Theory of Machines", subjects.get(4).getSubjectName());
        assertEquals("A", subjects.get(4).getGrade());

        // Verify subject 6
        assertEquals("ME23431", subjects.get(5).getSubjectCode());
        assertEquals("Thermal Engineering", subjects.get(5).getSubjectName());
        assertEquals("O", subjects.get(5).getGrade());

        // Verify subject 7
        assertEquals("ME23421", subjects.get(6).getSubjectCode());
        assertEquals("Manufacturing Technology Laboratory", subjects.get(6).getSubjectName());
        assertEquals("A", subjects.get(6).getGrade());

        // Verify subject 8 (multi-line laboratory title)
        assertEquals("ME23422", subjects.get(7).getSubjectCode());
        assertEquals("Strength of Materials and Fluid Machinery Laboratory", subjects.get(7).getSubjectName());
        assertEquals("O", subjects.get(7).getGrade());

        // Verify subject 9 (multi-line management title)
        assertEquals("ME23IC1", subjects.get(8).getSubjectCode());
        assertEquals("Introduction to Product Lifecycle Management", subjects.get(8).getSubjectName());
        assertEquals("O", subjects.get(8).getGrade());
        assertEquals("PASS", subjects.get(8).getResultStatus());
    }

    @Test
    @DisplayName("Extract Semester 03 PDF accurately (3.pdf)")
    void testExtractSemester03Pdf() throws IOException {
        File sampleFile = new File("../sample-docs/3.pdf");
        if (!sampleFile.exists()) {
            sampleFile = new File("sample-docs/3.pdf");
        }
        assertTrue(sampleFile.exists(), "3.pdf must exist at " + sampleFile.getAbsolutePath());

        byte[] bytes = Files.readAllBytes(sampleFile.toPath());
        ExtractedResultResponse response = pdfExtractionService.extractFromBytes(bytes);

        assertTrue(response.isSuccess(), "Extraction should succeed. Warnings: " + response.getWarnings());
        assertEquals("R KATHIRVELAN", response.getStudentName());
        assertEquals("2117240060037", response.getRegisterNumber());
        assertEquals("2025-2026", response.getAcademicYear());
        assertEquals("03", response.getSemester());
        assertEquals("B.E. MECH", response.getCourse());
        assertTrue(response.getWarnings().isEmpty(),
                "Expected zero extraction warnings, but got: " + response.getWarnings());

        List<ExtractedSubjectDto> subjects = response.getSubjects();
        assertEquals(8, subjects.size(), "Should extract exactly 8 subjects from 3.pdf");

        // Row 1: CS23312
        assertEquals("CS23312", subjects.get(0).getSubjectCode());
        assertEquals("Object Oriented Programming", subjects.get(0).getSubjectName());
        assertEquals("A", subjects.get(0).getGrade());
        assertEquals("PASS", subjects.get(0).getResultStatus());
        assertEquals("03", subjects.get(0).getSubjectSemester());
        assertEquals("November 2025", subjects.get(0).getExamDate());

        // Row 2: MA23313 (Multi-line title: Transforms and Partial Differential +
        // Equations)
        assertEquals("MA23313", subjects.get(1).getSubjectCode());
        assertEquals("Transforms and Partial Differential Equations", subjects.get(1).getSubjectName());
        assertEquals("B+", subjects.get(1).getGrade());
        assertEquals("PASS", subjects.get(1).getResultStatus());
        assertEquals("03", subjects.get(1).getSubjectSemester());

        // Row 3: ME23311
        assertEquals("ME23311", subjects.get(2).getSubjectCode());
        assertEquals("Engineering Mechanics", subjects.get(2).getSubjectName());
        assertEquals("A+", subjects.get(2).getGrade());
        assertEquals("PASS", subjects.get(2).getResultStatus());

        // Row 4: ME23312
        assertEquals("ME23312", subjects.get(3).getSubjectCode());
        assertEquals("Engineering Thermodynamics", subjects.get(3).getSubjectName());
        assertEquals("A+", subjects.get(3).getGrade());
        assertEquals("PASS", subjects.get(3).getResultStatus());

        // Row 5: ME23313
        assertEquals("ME23313", subjects.get(4).getSubjectCode());
        assertEquals("Fluid Mechanics and Machinery", subjects.get(4).getSubjectName());
        assertEquals("A", subjects.get(4).getGrade());
        assertEquals("PASS", subjects.get(4).getResultStatus());

        // Row 6: ME23314
        assertEquals("ME23314", subjects.get(5).getSubjectCode());
        assertEquals("Manufacturing Processes", subjects.get(5).getSubjectName());
        assertEquals("O", subjects.get(5).getGrade());
        assertEquals("PASS", subjects.get(5).getResultStatus());

        // Row 7: CS23322 (Multi-line title: Object Oriented Programming + Laboratory)
        assertEquals("CS23322", subjects.get(6).getSubjectCode());
        assertEquals("Object Oriented Programming Laboratory", subjects.get(6).getSubjectName());
        assertEquals("O", subjects.get(6).getGrade());
        assertEquals("PASS", subjects.get(6).getResultStatus());

        // Row 8: ME23321 (Multi-line title: Computer Aided Machine Drawing +
        // Laboratory; excludes bottom annotations)
        assertEquals("ME23321", subjects.get(7).getSubjectCode());
        assertEquals("Computer Aided Machine Drawing Laboratory", subjects.get(7).getSubjectName());
        assertEquals("O", subjects.get(7).getGrade());
        assertEquals("PASS", subjects.get(7).getResultStatus());
        assertEquals("03", subjects.get(7).getSubjectSemester());
        assertEquals("November 2025", subjects.get(7).getExamDate());
    }

    @Test
    @DisplayName("Reject non-RIT or corrupted document")
    void testRejectInvalidDocument() {
        byte[] dummyBytes = "This is not a valid RIT result PDF document.".getBytes();
        ExtractedResultResponse response = assertDoesNotThrow(() -> pdfExtractionService.extractFromBytes(dummyBytes));
        assertFalse(response.isSuccess());
    }
}
