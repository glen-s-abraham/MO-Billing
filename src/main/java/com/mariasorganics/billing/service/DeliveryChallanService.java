package com.mariasorganics.billing.service;

import com.mariasorganics.billing.model.*;
import com.mariasorganics.billing.repository.DocumentConfigurationRepository;
import com.mariasorganics.billing.repository.DeliveryChallanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryChallanService {
    private final DeliveryChallanRepository deliveryChallanRepository;
    private final DocumentConfigurationRepository docConfigRepo;

    public List<DeliveryChallan> getAllDeliveryChallans() {
        return deliveryChallanRepository.findAll();
    }

    public DeliveryChallan getDeliveryChallanById(Long id) {
        return deliveryChallanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid delivery challan ID: " + id));
    }

    @Transactional
    public DeliveryChallan saveDeliveryChallan(DeliveryChallan deliveryChallan) {
        if (deliveryChallan.getId() == null && (deliveryChallan.getChallanNumber() == null || deliveryChallan.getChallanNumber().isEmpty())) {
            deliveryChallan.setChallanNumber(generateChallanNumber());
        }

        BigDecimal grandTotal = BigDecimal.ZERO;
        if (deliveryChallan.getItems() != null) {
            for (DeliveryChallanItem item : deliveryChallan.getItems()) {
                if (item.getQuantity() != null && item.getRate() != null) {
                    BigDecimal rowTotal = item.getQuantity().multiply(item.getRate());
                    item.setRowTotal(rowTotal);
                    grandTotal = grandTotal.add(rowTotal);
                }
                item.setDeliveryChallanEntity(deliveryChallan);
            }
        }
        deliveryChallan.setTotalAmount(grandTotal);

        return deliveryChallanRepository.save(deliveryChallan);
    }

    private String generateChallanNumber() {
        DocumentConfiguration config = docConfigRepo.findByDocumentType(DocumentType.DELIVERY_CHALLAN)
                .orElse(new DocumentConfiguration());
        String prefix = config.getDocumentPrefix();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "DC-";
        }

        long count = deliveryChallanRepository.count() + 1;
        return prefix + String.format("%04d", count);
    }
}
