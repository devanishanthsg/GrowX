package com.growx.service;

import com.growx.dto.response.ReportResponse;
import com.growx.entity.Report;
import com.growx.entity.Farm;
import com.growx.enums.ReportType;
import com.growx.exception.ResourceNotFoundException;
import com.growx.mapper.ReportMapper;
import com.growx.repository.FarmRepository;
import com.growx.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Manages farm reports: listing, generating, and downloading.
 * PDF generation support is architecturally prepared but not yet implemented.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final FarmRepository farmRepository;
    private final ReportMapper reportMapper;

    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsForFarm(Long farmId, Long userId) {
        farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", farmId));

        return reportRepository.findByFarmIdOrderByCreatedAtDesc(farmId)
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    /**
     * Returns aggregate counts for the Reports page stat boxes:
     * totalReports, aiPredictions, diseaseAnalyses.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getReportStats(Long farmId, Long userId) {
        farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", farmId));

        long total = reportRepository.countByFarmId(farmId);
        long aiPredictions = reportRepository.countByFarmIdAndType(farmId, ReportType.CROP_RECOMMENDATION);
        long diseaseAnalyses = reportRepository.countByFarmIdAndType(farmId, ReportType.DISEASE_DETECTION);

        return Map.of(
                "totalReports", total,
                "aiPredictions", aiPredictions,
                "diseaseAnalyses", diseaseAnalyses
        );
    }

    @Transactional
    public ReportResponse generateReport(Long farmId, ReportType type, Long userId) {
        Farm farm = farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", farmId));

        Report report = Report.builder()
                .title(buildReportTitle(type, farm.getFarmName()))
                .type(type)
                .status("Completed")
                .farm(farm)
                .build();

        // TODO: Add PDF generation here when library is integrated
        // String pdfPath = pdfService.generatePdf(report);
        // report.setFilePath(pdfPath);

        return reportMapper.toResponse(reportRepository.save(report));
    }

    private String buildReportTitle(ReportType type, String farmName) {
        return switch (type) {
            case CROP_RECOMMENDATION -> "Crop Recommendation Report — " + farmName;
            case DISEASE_DETECTION -> "Plant Disease Analysis — " + farmName;
            case FARM_SUMMARY -> "Monthly Farm Summary — " + farmName;
            case WEATHER -> "Weather Report — " + farmName;
        };
    }
}
