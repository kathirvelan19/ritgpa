package com.rit.gpa.dto;

import java.util.List;

public class CalculateGpaResponse {
    private Double sgpa;
    private Double totalCredits;
    private Double totalGradePoints;
    private Double passedCredits;
    private int totalSubjects;
    private int arrearCount;
    private List<SubjectGradeInput> subjects;
    private String status;

    public CalculateGpaResponse() {}

    public Double getSgpa() {
        return sgpa;
    }

    public void setSgpa(Double sgpa) {
        this.sgpa = sgpa;
    }

    public Double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public Double getTotalGradePoints() {
        return totalGradePoints;
    }

    public void setTotalGradePoints(Double totalGradePoints) {
        this.totalGradePoints = totalGradePoints;
    }

    public Double getPassedCredits() {
        return passedCredits;
    }

    public void setPassedCredits(Double passedCredits) {
        this.passedCredits = passedCredits;
    }

    public int getTotalSubjects() {
        return totalSubjects;
    }

    public void setTotalSubjects(int totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public int getArrearCount() {
        return arrearCount;
    }

    public void setArrearCount(int arrearCount) {
        this.arrearCount = arrearCount;
    }

    public List<SubjectGradeInput> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectGradeInput> subjects) {
        this.subjects = subjects;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

