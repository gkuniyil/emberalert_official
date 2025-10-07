package com.emberalert.repository;

import com.emberalert.entity.FirePrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for FirePrediction entity
 * Spring Boot auto-generates all database methods!
 */
@Repository
public interface FirePredictionRepository extends JpaRepository<FirePrediction, Long> {
    
    /**
     * Find predictions by location (within a radius)
     * Custom query method
     */
    @Query("SELECT p FROM FirePrediction p WHERE " +
           "p.latitude BETWEEN :lat - 0.1 AND :lat + 0.1 AND " +
           "p.longitude BETWEEN :lon - 0.1 AND :lon + 0.1 " +
           "ORDER BY p.createdAt DESC")
    List<FirePrediction> findByLocationNear(@Param("lat") Double latitude, 
                                            @Param("lon") Double longitude);
    
    /**
     * Find predictions by risk category
     */
    List<FirePrediction> findByRiskCategory(String riskCategory);
    
    /**
     * Find predictions with risk score above threshold
     */
    List<FirePrediction> findByRiskScoreGreaterThan(Double threshold);
    
    /**
     * Find recent predictions (last N hours)
     */
    @Query("SELECT p FROM FirePrediction p WHERE p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<FirePrediction> findRecentPredictions(@Param("since") LocalDateTime since);
    
    /**
     * Count predictions by risk category
     */
    Long countByRiskCategory(String riskCategory);
    
    /**
     * Find all predictions ordered by date (newest first)
     */
    List<FirePrediction> findAllByOrderByCreatedAtDesc();
}