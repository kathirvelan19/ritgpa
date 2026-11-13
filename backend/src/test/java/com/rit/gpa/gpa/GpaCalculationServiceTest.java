package com.rit.gpa.gpa;

import com.rit.gpa.dto.CalculateGpaRequest;
import com.rit.gpa.dto.CalculateGpaResponse;
import com.rit.gpa.dto.SemesterRecordDto;
import com.rit.gpa.dto.SubjectGradeInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GpaCalculationServiceTest {

    private GpaCalculationService service;
    private GradePointConfig config;

    @BeforeEach
    void setUp() {
        config = new GradePointConfig();
        service = new GpaCalculationService(config);
    }

    @Test
    @DisplayName("Verify A+ gives 9.0 and A gives 8.0, and they are not equal")
    void testGradePointsDistinction() {
        assertEquals(9.0, config.getGradePoint("A+"));
        assertEquals(8.0, config.getGradePoint("A"));
        assertNotEquals(config.getGradePoint("A+"), config.getGradePoint("A"));
        assertEquals(10.0, config.getGradePoint("O"));
        assertEquals(7.0, config.getGradePoint("B+"));
        assertEquals(6.0, config.getGradePoint("B"));
        assertEquals(5.0, config.getGradePoint("C"));
        assertEquals(0.0, config.getGradePoint("U"));
    }

    @Test
    @DisplayName("Calculate normal semester SGPA with all passing grades")
    void testNormalSemesterCalculation() {
        CalculateGpaRequest request = new CalculateGpaRequest();
        List<SubjectGradeInput> subjects = Arrays.asList(
                new SubjectGradeInput("GE23412", "Environmental Science", "O", 3.0, "PASS"), // 3 * 10 = 30
                new SubjectGradeInput("ME23411", "Engineering Materials", "O", 3.0, "PASS"), // 3 * 10 = 30
                new SubjectGradeInput("ME23412", "Manufacturing Tech", "A+", 4.0, "PASS"),  // 4 * 9 = 36
                new SubjectGradeInput("ME23413", "Mechanics of Solids", "A", 3.0, "PASS")   // 3 * 8 = 24
        );
        // Total points = 30 + 30 + 36 + 24 = 120
        // Total credits = 3 + 3 + 4 + 3 = 13
        // SGPA = 120 / 13 = 9.2307... -> 9.23
        request.setSubjects(subjects);

        CalculateGpaResponse response = service.calculateSemesterGpa(request);

        assertNotNull(response);
        assertEquals(9.23, response.getSgpa());
        assertEquals(13.0, response.getTotalCredits());
        assertEquals(120.0, response.getTotalGradePoints());
        assertEquals(13.0, response.getPassedCredits());
        assertEquals(0, response.getArrearCount());
        assertEquals("PASSED", response.getStatus());
    }

    @Test
    @DisplayName("Calculate semester SGPA with an arrear (grade U = 0 points)")
    void testSemesterWithArrear() {
        CalculateGpaRequest request = new CalculateGpaRequest();
        List<SubjectGradeInput> subjects = Arrays.asList(
                new SubjectGradeInput("SUB101", "Maths", "O", 4.0, "PASS"), // 4 * 10 = 40
                new SubjectGradeInput("SUB102", "Physics", "U", 3.0, "RA")   // 3 * 0 = 0
        );
        // Total points = 40
        // Total credits = 7.0
        // SGPA = 40 / 7 = 5.714... -> 5.71
        // Passed credits = 4.0
        request.setSubjects(subjects);

        CalculateGpaResponse response = service.calculateSemesterGpa(request);

        assertEquals(5.71, response.getSgpa());
        assertEquals(7.0, response.getTotalCredits());
        assertEquals(4.0, response.getPassedCredits());
        assertEquals(1, response.getArrearCount());
        assertEquals("ARREARS_PENDING", response.getStatus());
    }

    @Test
    @DisplayName("Validation should reject invalid grade")
    void testInvalidGrade() {
        CalculateGpaRequest request = new CalculateGpaRequest();
        request.setSubjects(List.of(
                new SubjectGradeInput("SUB101", "Maths", "XYZ", 4.0, "PASS")
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.calculateSemesterGpa(request));
        assertTrue(ex.getMessage().contains("Invalid grade"));
    }

    @Test
    @DisplayName("Validation should reject zero or negative credits")
    void testZeroCredits() {
        CalculateGpaRequest request = new CalculateGpaRequest();
        request.setSubjects(List.of(
                new SubjectGradeInput("SUB101", "Maths", "O", 0.0, "PASS")
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.calculateSemesterGpa(request));
        assertTrue(ex.getMessage().contains("must be positive"));
    }

    @Test
    @DisplayName("Calculate cumulative CGPA across multiple semesters correctly (credit-weighted)")
    void testCumulativeCgpa() {
        SemesterRecordDto sem1 = new SemesterRecordDto();
        sem1.setSemesterNumber("1");
        sem1.setSubjects(Arrays.asList(
                new SubjectGradeInput("S1", "Sub1", "O", 10.0, "PASS"), // 10 * 10 = 100
                new SubjectGradeInput("S2", "Sub2", "A", 10.0, "PASS")  // 10 * 8 = 80
        )); // Sem 1: 180 points / 20 credits = 9.00 SGPA

        SemesterRecordDto sem2 = new SemesterRecordDto();
        sem2.setSemesterNumber("2");
        sem2.setSubjects(Arrays.asList(
                new SubjectGradeInput("S3", "Sub3", "A+", 20.0, "PASS") // 20 * 9 = 180
        )); // Sem 2: 180 points / 20 credits = 9.00 SGPA

        // Total weighted points = 180 + 180 = 360
        // Total credits = 20 + 20 = 40
        // CGPA = 360 / 40 = 9.00
        Double cgpa = service.calculateCumulativeGpa(Arrays.asList(sem1, sem2));
        assertEquals(9.00, cgpa);
    }
}

