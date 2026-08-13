package com.growx.repository;

import com.growx.entity.Report;
import com.growx.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for the Report entity.
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByFarmIdOrderByCreatedAtDesc(Long farmId);

    long countByFarmId(Long farmId);

    long countByFarmIdAndType(Long farmId, ReportType type);
}
