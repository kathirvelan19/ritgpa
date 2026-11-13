package com.rit.gpa.controller;

import com.rit.gpa.dto.CalculateGpaRequest;
import com.rit.gpa.dto.CalculateGpaResponse;
import com.rit.gpa.dto.GpaSummaryDto;
import com.rit.gpa.gpa.GpaCalculationService;
import com.rit.gpa.security.FirebaseUserPrincipal;
import com.rit.gpa.service.SemesterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gpa")
public class GpaCalculationController {

    private final GpaCalculationService gpaCalculationService;
    private final SemesterService semesterService;

    public GpaCalculationController(GpaCalculationService gpaCalculationService, SemesterService semesterService) {
        this.gpaCalculationService = gpaCalculationService;
        this.semesterService = semesterService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<CalculateGpaResponse> calculateGpa(
            @Valid @RequestBody CalculateGpaRequest request,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        CalculateGpaResponse response = gpaCalculationService.calculateSemesterGpa(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<GpaSummaryDto> getSummary(@AuthenticationPrincipal FirebaseUserPrincipal principal) {
        String uid = principal != null ? principal.getUid() : "anonymous";
        GpaSummaryDto summary = semesterService.getGpaSummary(uid);
        return ResponseEntity.ok(summary);
    }
}

