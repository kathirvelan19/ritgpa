package com.rit.gpa.controller;

import com.rit.gpa.dto.ExtractedResultResponse;
import com.rit.gpa.pdf.PdfExtractionService;
import com.rit.gpa.security.FirebaseUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
public class PdfExtractionController {
    private static final Logger log = LoggerFactory.getLogger(PdfExtractionController.class);

    private final PdfExtractionService pdfExtractionService;

    public PdfExtractionController(PdfExtractionService pdfExtractionService) {
        this.pdfExtractionService = pdfExtractionService;
    }

    @PostMapping("/extract")
    public ResponseEntity<ExtractedResultResponse> extractPdf(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        log.info("PDF extraction requested by student UID: {}", principal != null ? principal.getUid() : "unauthenticated");

        try {
            ExtractedResultResponse response = pdfExtractionService.extractFromPdf(file);
            if (!response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ExtractedResultResponse errorResponse = new ExtractedResultResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.getWarnings().add(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during PDF extraction: ", e);
            ExtractedResultResponse errorResponse = new ExtractedResultResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Internal server error during PDF extraction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

