package com.mariasorganics.billing.repository;

import com.mariasorganics.billing.model.DeliveryChallanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryChallanItemRepository extends JpaRepository<DeliveryChallanItem, Long> {
}
