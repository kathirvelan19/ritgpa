package com.rit.gpa.dto;

public class ExtractedSubjectDto {
    private String subjectSemester;
    private String subjectCode;
    private String subjectName;
    private String grade;
    private String resultStatus;
    private String examDate;
    private Double suggestedCredits;
    private boolean uncertain;
    private String warningMessage;

    public ExtractedSubjectDto() {}

    public ExtractedSubjectDto(String subjectSemester, String subjectCode, String subjectName,
                               String grade, String resultStatus, String examDate) {
        this.subjectSemester = subjectSemester;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.grade = grade;
        this.resultStatus = resultStatus;
        this.examDate = examDate;
        this.suggestedCredits = null; // As per project rules: never guess or auto-invent credits!
        this.uncertain = false;
    }

    public String getSubjectSemester() {
        return subjectSemester;
    }

    public void setSubjectSemester(String subjectSemester) {
        this.subjectSemester = subjectSemester;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public Double getSuggestedCredits() {
        return suggestedCredits;
    }

    public void setSuggestedCredits(Double suggestedCredits) {
        this.suggestedCredits = suggestedCredits;
    }

    public boolean isUncertain() {
        return uncertain;
    }

    public void setUncertain(boolean uncertain) {
        this.uncertain = uncertain;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }
}

