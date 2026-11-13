package com.rit.gpa.controller;

import com.rit.gpa.dto.SemesterRecordDto;
import com.rit.gpa.security.FirebaseUserPrincipal;
import com.rit.gpa.service.SemesterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @PostMapping
    public ResponseEntity<SemesterRecordDto> saveSemester(
            @RequestBody SemesterRecordDto record,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        String uid = principal != null ? principal.getUid() : "anonymous";
        SemesterRecordDto saved = semesterService.saveSemester(uid, record);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<SemesterRecordDto>> getAllSemesters(
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        String uid = principal != null ? principal.getUid() : "anonymous";
        List<SemesterRecordDto> list = semesterService.getSemestersForUser(uid);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{semesterId}")
    public ResponseEntity<SemesterRecordDto> getSemester(
            @PathVariable("semesterId") String semesterId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        String uid = principal != null ? principal.getUid() : "anonymous";
        SemesterRecordDto record = semesterService.getSemesterById(uid, semesterId);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }
}

