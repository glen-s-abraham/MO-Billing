package com.mariasorganics.billing.repository;

import com.mariasorganics.billing.model.DeliveryChallan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryChallanRepository extends JpaRepository<DeliveryChallan, Long> {
    long countByBuyerEntityAndStatusNot(com.mariasorganics.billing.model.Buyer buyer, com.mariasorganics.billing.model.DeliveryChallanStatus status);
    long countByStatusNot(com.mariasorganics.billing.model.DeliveryChallanStatus status);
}
