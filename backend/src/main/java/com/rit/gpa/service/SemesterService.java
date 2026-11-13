package com.rit.gpa.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.rit.gpa.dto.CalculateGpaResponse;
import com.rit.gpa.dto.GpaSummaryDto;
import com.rit.gpa.dto.SemesterRecordDto;
import com.rit.gpa.dto.SubjectGradeInput;
import com.rit.gpa.gpa.GpaCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SemesterService {
    private static final Logger log = LoggerFactory.getLogger(SemesterService.class);

    private final Firestore firestore;
    private final GpaCalculationService gpaCalculationService;

    // In-memory fallback cache by UID -> semesterId -> SemesterRecordDto
    // Ensures zero-downtime operation even before Google Cloud service account keys are placed locally
    private final Map<String, Map<String, SemesterRecordDto>> fallbackStorage = new ConcurrentHashMap<>();

    public SemesterService(@Autowired(required = false) Firestore firestore,
                           GpaCalculationService gpaCalculationService) {
        this.firestore = firestore;
        this.gpaCalculationService = gpaCalculationService;
    }

    public SemesterRecordDto saveSemester(String uid, SemesterRecordDto dto) {
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("Authenticated user ID is required.");
        }

        // Validate and recalculate GPA to ensure authenticity (Rule: Never trust client-calculated GPA)
        com.rit.gpa.dto.CalculateGpaRequest calcReq = new com.rit.gpa.dto.CalculateGpaRequest();
        calcReq.setSubjects(dto.getSubjects());
        CalculateGpaResponse calcRes = gpaCalculationService.calculateSemesterGpa(calcReq);

        dto.setStudentUid(uid);
        dto.setSgpa(calcRes.getSgpa());
        dto.setTotalCredits(calcRes.getTotalCredits());
        dto.setPassedCredits(calcRes.getPassedCredits());
        dto.setArrearCount(calcRes.getArrearCount());
        dto.setSubjects(calcRes.getSubjects());

        long now = System.currentTimeMillis();
        if (dto.getCreatedAt() == null) {
            dto.setCreatedAt(now);
        }
        dto.setUpdatedAt(now);

        String semId = dto.getId();
        if (semId == null || semId.trim().isEmpty()) {
            semId = "sem_" + (dto.getSemesterNumber() != null ? dto.getSemesterNumber() : UUID.randomUUID().toString().substring(0, 8));
            dto.setId(semId);
        }

        // Save to Firestore if available
        if (firestore != null) {
            try {
                DocumentReference docRef = firestore.collection("users").document(uid)
                        .collection("semesters").document(semId);
                docRef.set(dto).get();
                log.info("Saved semester {} for UID {} to Firestore", semId, uid);
            } catch (Exception e) {
                log.warn("Firestore save failed, falling back to local storage: {}", e.getMessage());
                saveToFallback(uid, dto);
            }
        } else {
            saveToFallback(uid, dto);
        }

        // Recalculate and attach overall CGPA
        List<SemesterRecordDto> allSemesters = getSemestersForUser(uid);
        Double cgpa = gpaCalculationService.calculateCumulativeGpa(allSemesters);
        dto.setCgpa(cgpa);

        return dto;
    }

    public List<SemesterRecordDto> getSemestersForUser(String uid) {
        if (uid == null) return Collections.emptyList();

        List<SemesterRecordDto> list = new ArrayList<>();

        if (firestore != null) {
            try {
                CollectionReference colRef = firestore.collection("users").document(uid).collection("semesters");
                ApiFuture<QuerySnapshot> future = colRef.get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                for (QueryDocumentSnapshot doc : docs) {
                    SemesterRecordDto item = doc.toObject(SemesterRecordDto.class);
                    list.add(item);
                }
            } catch (Exception e) {
                log.warn("Could not query Firestore for semesters: {}", e.getMessage());
            }
        }

        if (list.isEmpty()) {
            Map<String, SemesterRecordDto> userSemesters = fallbackStorage.get(uid);
            if (userSemesters != null) {
                list.addAll(userSemesters.values());
            }
        }

        // Sort by semester number
        list.sort(Comparator.comparing(s -> {
            try {
                return Integer.parseInt(s.getSemesterNumber());
            } catch (Exception e) {
                return 0;
            }
        }));

        // Calculate and set CGPA on each record
        Double cgpa = gpaCalculationService.calculateCumulativeGpa(list);
        for (SemesterRecordDto s : list) {
            s.setCgpa(cgpa);
        }

        return list;
    }

    public SemesterRecordDto getSemesterById(String uid, String semesterId) {
        if (uid == null || semesterId == null) return null;

        if (firestore != null) {
            try {
                DocumentSnapshot doc = firestore.collection("users").document(uid)
                        .collection("semesters").document(semesterId).get().get();
                if (doc.exists()) {
                    SemesterRecordDto item = doc.toObject(SemesterRecordDto.class);
                    List<SemesterRecordDto> all = getSemestersForUser(uid);
                    if (item != null) {
                        item.setCgpa(gpaCalculationService.calculateCumulativeGpa(all));
                    }
                    return item;
                }
            } catch (Exception e) {
                log.warn("Could not get semester from Firestore: {}", e.getMessage());
            }
        }

        Map<String, SemesterRecordDto> map = fallbackStorage.get(uid);
        if (map != null && map.containsKey(semesterId)) {
            SemesterRecordDto item = map.get(semesterId);
            List<SemesterRecordDto> all = getSemestersForUser(uid);
            item.setCgpa(gpaCalculationService.calculateCumulativeGpa(all));
            return item;
        }

        return null;
    }

    public GpaSummaryDto getGpaSummary(String uid) {
        List<SemesterRecordDto> semesters = getSemestersForUser(uid);

        GpaSummaryDto summary = new GpaSummaryDto();
        if (semesters.isEmpty()) {
            summary.setCompletedSemesters(0);
            summary.setLatestSgpa(null);
            summary.setOverallCgpa(null);
            summary.setTotalCredits(0.0);
            summary.setTotalPassedCredits(0.0);
            summary.setTotalArrears(0);
            summary.setHistory(Collections.emptyList());
            return summary;
        }

        double totalCredits = 0.0;
        double totalPassedCredits = 0.0;
        int totalArrears = 0;
        List<GpaSummaryDto.SemesterHistoryItem> history = new ArrayList<>();

        for (SemesterRecordDto s : semesters) {
            if (s.getTotalCredits() != null) totalCredits += s.getTotalCredits();
            if (s.getPassedCredits() != null) totalPassedCredits += s.getPassedCredits();
            totalArrears += s.getArrearCount();

            history.add(new GpaSummaryDto.SemesterHistoryItem(
                    s.getId(),
                    s.getSemesterNumber(),
                    s.getAcademicYear(),
                    s.getSgpa(),
                    s.getTotalCredits(),
                    s.getSubjects() != null ? s.getSubjects().size() : 0,
                    s.getArrearCount()
            ));
        }

        SemesterRecordDto latest = semesters.get(semesters.size() - 1);

        summary.setLatestSgpa(latest.getSgpa());
        summary.setOverallCgpa(gpaCalculationService.calculateCumulativeGpa(semesters));
        summary.setTotalCredits(GpaCalculationService.roundToTwoDecimals(totalCredits));
        summary.setTotalPassedCredits(GpaCalculationService.roundToTwoDecimals(totalPassedCredits));
        summary.setCompletedSemesters(semesters.size());
        summary.setTotalArrears(totalArrears);
        summary.setHistory(history);

        return summary;
    }

    private void saveToFallback(String uid, SemesterRecordDto dto) {
        fallbackStorage.computeIfAbsent(uid, k -> new ConcurrentHashMap<>()).put(dto.getId(), dto);
    }
}

