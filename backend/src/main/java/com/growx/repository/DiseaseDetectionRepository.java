package com.growx.repository;

import com.growx.entity.DiseaseDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for the DiseaseDetection entity.
 */
@Repository
public interface DiseaseDetectionRepository extends JpaRepository<DiseaseDetection, Long> {

    List<DiseaseDetection> findByFarmIdOrderByDetectedAtDesc(Long farmId);

    long countByFarmId(Long farmId);
}
