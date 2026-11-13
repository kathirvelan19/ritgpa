package com.rit.gpa.dto;

import java.util.List;

public class SemesterRecordDto {
    private String id;
    private String studentUid;
    private String studentName;
    private String registerNumber;
    private String academicYear;
    private String semesterNumber;
    private String course;
    private List<SubjectGradeInput> subjects;
    private Double totalCredits;
    private Double passedCredits;
    private Double sgpa;
    private Double cgpa;
    private int arrearCount;
    private String extractionStatus;
    private Long createdAt;
    private Long updatedAt;

    public SemesterRecordDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(String studentUid) {
        this.studentUid = studentUid;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(String semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public List<SubjectGradeInput> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectGradeInput> subjects) {
        this.subjects = subjects;
    }

    public Double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public Double getPassedCredits() {
        return passedCredits;
    }

    public void setPassedCredits(Double passedCredits) {
        this.passedCredits = passedCredits;
    }

    public Double getSgpa() {
        return sgpa;
    }

    public void setSgpa(Double sgpa) {
        this.sgpa = sgpa;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public int getArrearCount() {
        return arrearCount;
    }

    public void setArrearCount(int arrearCount) {
        this.arrearCount = arrearCount;
    }

    public String getExtractionStatus() {
        return extractionStatus;
    }

    public void setExtractionStatus(String extractionStatus) {
        this.extractionStatus = extractionStatus;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}

