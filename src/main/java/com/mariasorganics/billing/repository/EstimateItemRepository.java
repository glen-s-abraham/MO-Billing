package com.mariasorganics.billing.repository;

import com.mariasorganics.billing.model.EstimateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstimateItemRepository extends JpaRepository<EstimateItem, Long> {
}
