package com.rit.gpa.dto;

import java.util.List;

public class GpaSummaryDto {
    private Double latestSgpa;
    private Double overallCgpa;
    private Double totalCredits;
    private Double totalPassedCredits;
    private int completedSemesters;
    private int totalArrears;
    private List<SemesterHistoryItem> history;

    public static class SemesterHistoryItem {
        private String semesterId;
        private String semesterNumber;
        private String academicYear;
        private Double sgpa;
        private Double credits;
        private int subjectCount;
        private int arrears;

        public SemesterHistoryItem() {}

        public SemesterHistoryItem(String semesterId, String semesterNumber, String academicYear,
                                   Double sgpa, Double credits, int subjectCount, int arrears) {
            this.semesterId = semesterId;
            this.semesterNumber = semesterNumber;
            this.academicYear = academicYear;
            this.sgpa = sgpa;
            this.credits = credits;
            this.subjectCount = subjectCount;
            this.arrears = arrears;
        }

        public String getSemesterId() {
            return semesterId;
        }

        public void setSemesterId(String semesterId) {
            this.semesterId = semesterId;
        }

        public String getSemesterNumber() {
            return semesterNumber;
        }

        public void setSemesterNumber(String semesterNumber) {
            this.semesterNumber = semesterNumber;
        }

        public String getAcademicYear() {
            return academicYear;
        }

        public void setAcademicYear(String academicYear) {
            this.academicYear = academicYear;
        }

        public Double getSgpa() {
            return sgpa;
        }

        public void setSgpa(Double sgpa) {
            this.sgpa = sgpa;
        }

        public Double getCredits() {
            return credits;
        }

        public void setCredits(Double credits) {
            this.credits = credits;
        }

        public int getSubjectCount() {
            return subjectCount;
        }

        public void setSubjectCount(int subjectCount) {
            this.subjectCount = subjectCount;
        }

        public int getArrears() {
            return arrears;
        }

        public void setArrears(int arrears) {
            this.arrears = arrears;
        }
    }

    public GpaSummaryDto() {}

    public Double getLatestSgpa() {
        return latestSgpa;
    }

    public void setLatestSgpa(Double latestSgpa) {
        this.latestSgpa = latestSgpa;
    }

    public Double getOverallCgpa() {
        return overallCgpa;
    }

    public void setOverallCgpa(Double overallCgpa) {
        this.overallCgpa = overallCgpa;
    }

    public Double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public Double getTotalPassedCredits() {
        return totalPassedCredits;
    }

    public void setTotalPassedCredits(Double totalPassedCredits) {
        this.totalPassedCredits = totalPassedCredits;
    }

    public int getCompletedSemesters() {
        return completedSemesters;
    }

    public void setCompletedSemesters(int completedSemesters) {
        this.completedSemesters = completedSemesters;
    }

    public int getTotalArrears() {
        return totalArrears;
    }

    public void setTotalArrears(int totalArrears) {
        this.totalArrears = totalArrears;
    }

    public List<SemesterHistoryItem> getHistory() {
        return history;
    }

    public void setHistory(List<SemesterHistoryItem> history) {
        this.history = history;
    }
}

