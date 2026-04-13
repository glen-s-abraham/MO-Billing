package com.mariasorganics.billing.service;

import com.mariasorganics.billing.model.*;
import com.mariasorganics.billing.repository.DocumentConfigurationRepository;
import com.mariasorganics.billing.repository.EstimateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstimateService {
    private final EstimateRepository estimateRepository;
    private final DocumentConfigurationRepository docConfigRepo;

    public List<Estimate> getAllEstimates() {
        return estimateRepository.findAll();
    }

    public Estimate getEstimateById(Long id) {
        return estimateRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid estimate ID: " + id));
    }

    @Transactional
    public Estimate saveEstimate(Estimate estimate) {
        if (estimate.getId() == null && (estimate.getEstimateNumber() == null || estimate.getEstimateNumber().isEmpty())) {
            estimate.setEstimateNumber(generateEstimateNumber());
        }

        BigDecimal grandTotal = BigDecimal.ZERO;
        if (estimate.getItems() != null) {
            for (EstimateItem item : estimate.getItems()) {
                if(item.getQuantity() != null && item.getRate() != null) {
                    BigDecimal rowTotal = item.getQuantity().multiply(item.getRate());
                    item.setRowTotal(rowTotal);
                    grandTotal = grandTotal.add(rowTotal);
                }
                item.setEstimateEntity(estimate);
            }
        }
        estimate.setTotalAmount(grandTotal);

        return estimateRepository.save(estimate);
    }

    private String generateEstimateNumber() {
        DocumentConfiguration config = docConfigRepo.findByDocumentType(DocumentType.ESTIMATE)
                .orElse(new DocumentConfiguration());
        String prefix = config.getDocumentPrefix();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "EST-";
        }

        long count = estimateRepository.count() + 1;
        return prefix + String.format("%04d", count);
    }
}
