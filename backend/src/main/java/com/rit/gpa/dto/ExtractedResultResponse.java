package com.rit.gpa.dto;

import java.util.ArrayList;
import java.util.List;

public class ExtractedResultResponse {
    private boolean success;
    private String message;
    private String studentName;
    private String registerNumber;
    private String academicYear;
    private String semester;
    private String course;
    private List<ExtractedSubjectDto> subjects = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public ExtractedResultResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public List<ExtractedSubjectDto> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<ExtractedSubjectDto> subjects) {
        this.subjects = subjects;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}

