package com.growx.controller;

import com.growx.dto.response.ApiResponse;
import com.growx.dto.response.ReportResponse;
import com.growx.enums.ReportType;
import com.growx.security.UserPrincipal;
import com.growx.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Report endpoints — all require a valid JWT.
 *
 * GET  /api/reports/farm/{farmId}          → list all reports for a farm
 * GET  /api/reports/farm/{farmId}/stats    → aggregate report counts for stat boxes
 * POST /api/reports/farm/{farmId}/generate → generate a new report
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports(
            @PathVariable Long farmId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<ReportResponse> reports = reportService.getReportsForFarm(farmId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @GetMapping("/farm/{farmId}/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getReportStats(
            @PathVariable Long farmId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Map<String, Long> stats = reportService.getReportStats(farmId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/farm/{farmId}/generate")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
            @PathVariable Long farmId,
            @RequestParam ReportType type,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ReportResponse report = reportService.generateReport(farmId, type, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report generated successfully.", report));
    }
}
