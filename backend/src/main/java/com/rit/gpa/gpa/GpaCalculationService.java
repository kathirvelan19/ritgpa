package com.rit.gpa.gpa;

import com.rit.gpa.dto.CalculateGpaRequest;
import com.rit.gpa.dto.CalculateGpaResponse;
import com.rit.gpa.dto.SemesterRecordDto;
import com.rit.gpa.dto.SubjectGradeInput;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class GpaCalculationService {
    private final GradePointConfig gradePointConfig;

    public GpaCalculationService(GradePointConfig gradePointConfig) {
        this.gradePointConfig = gradePointConfig;
    }

    public CalculateGpaResponse calculateSemesterGpa(CalculateGpaRequest request) {
        if (request == null || request.getSubjects() == null || request.getSubjects().isEmpty()) {
            throw new IllegalArgumentException("No subjects provided for GPA calculation.");
        }

        double totalCredits = 0.0;
        double totalWeightedPoints = 0.0;
        double passedCredits = 0.0;
        int arrearCount = 0;

        List<SubjectGradeInput> calculatedSubjects = new ArrayList<>();

        for (SubjectGradeInput subject : request.getSubjects()) {
            if (subject.getCredits() == null || subject.getCredits() <= 0) {
                throw new IllegalArgumentException("Credits for subject '" + subject.getSubjectCode() + "' must be positive.");
            }

            Double gp = gradePointConfig.getGradePoint(subject.getGrade());
            if (gp == null) {
                throw new IllegalArgumentException("Invalid grade '" + subject.getGrade() + "' for subject '" + subject.getSubjectCode() + "'.");
            }

            subject.setGradePoint(gp);

            totalCredits += subject.getCredits();
            totalWeightedPoints += (subject.getCredits() * gp);

            if (gradePointConfig.isArrear(subject.getGrade())) {
                arrearCount++;
            } else {
                passedCredits += subject.getCredits();
            }

            calculatedSubjects.add(subject);
        }

        if (totalCredits <= 0.0) {
            throw new IllegalArgumentException("Total credits cannot be zero.");
        }

        double rawSgpa = totalWeightedPoints / totalCredits;
        double roundedSgpa = roundToTwoDecimals(rawSgpa);

        CalculateGpaResponse response = new CalculateGpaResponse();
        response.setSgpa(roundedSgpa);
        response.setTotalCredits(roundToTwoDecimals(totalCredits));
        response.setTotalGradePoints(roundToTwoDecimals(totalWeightedPoints));
        response.setPassedCredits(roundToTwoDecimals(passedCredits));
        response.setTotalSubjects(calculatedSubjects.size());
        response.setArrearCount(arrearCount);
        response.setSubjects(calculatedSubjects);
        response.setStatus(arrearCount == 0 ? "PASSED" : "ARREARS_PENDING");

        return response;
    }

    public Double calculateCumulativeGpa(List<SemesterRecordDto> semesters) {
        if (semesters == null || semesters.isEmpty()) {
            return null;
        }

        double cumulativeWeightedPoints = 0.0;
        double cumulativeCredits = 0.0;

        for (SemesterRecordDto sem : semesters) {
            if (sem.getSubjects() == null) continue;

            for (SubjectGradeInput subject : sem.getSubjects()) {
                if (subject.getCredits() == null || subject.getCredits() <= 0) continue;
                Double gp = subject.getGradePoint();
                if (gp == null) {
                    gp = gradePointConfig.getGradePoint(subject.getGrade());
                }
                if (gp == null) continue;

                cumulativeCredits += subject.getCredits();
                cumulativeWeightedPoints += (subject.getCredits() * gp);
            }
        }

        if (cumulativeCredits <= 0.0) {
            return null;
        }

        return roundToTwoDecimals(cumulativeWeightedPoints / cumulativeCredits);
    }

    public static double roundToTwoDecimals(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

