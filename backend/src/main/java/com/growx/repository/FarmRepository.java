package com.growx.repository;

import com.growx.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for the Farm entity.
 */
@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {

    List<Farm> findByOwnerId(Long userId);

    Optional<Farm> findByIdAndOwnerId(Long farmId, Long userId);

    boolean existsByIdAndOwnerId(Long farmId, Long userId);
}
