package com.rit.gpa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CalculateGpaRequest {
    private String studentName;
    private String registerNumber;
    private String academicYear;
    private String semesterNumber;
    private String course;

    @NotEmpty(message = "Subject list must not be empty")
    @Valid
    private List<SubjectGradeInput> subjects;

    public CalculateGpaRequest() {}

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
}

