package com.growx.repository;

import com.growx.entity.CropRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for the CropRecommendation entity.
 */
@Repository
public interface CropRecommendationRepository extends JpaRepository<CropRecommendation, Long> {

    List<CropRecommendation> findByFarmIdOrderByCreatedAtDesc(Long farmId);

    long countByFarmId(Long farmId);
}
