package com.mariasorganics.billing.repository;

import com.mariasorganics.billing.model.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    long countByBuyerEntityAndStatusNot(com.mariasorganics.billing.model.Buyer buyer, com.mariasorganics.billing.model.EstimateStatus status);
    long countByStatusNot(com.mariasorganics.billing.model.EstimateStatus status);
    long countByBuyerEntityAndEstimateDateBetweenAndStatusNot(com.mariasorganics.billing.model.Buyer buyer, java.time.LocalDate startDate, java.time.LocalDate endDate, com.mariasorganics.billing.model.EstimateStatus status);
    long countByEstimateDateBetweenAndStatusNot(java.time.LocalDate startDate, java.time.LocalDate endDate, com.mariasorganics.billing.model.EstimateStatus status);
    boolean existsByEstimateNumber(String estimateNumber);
}
