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
                if (item.getProductEntity() != null && item.getQuantity() != null && item.getRate() != null) {
                    // 1. Fetch current product taxes for snapshotting (only for new items or re-calculating)
                    // In a more complex app, we might check if taxes changed, but for now we snapshot on save.
                    Product product = item.getProductEntity();
                    
                    // Clear existing snapshots to avoid duplicates on update
                    if (item.getTaxes() != null) {
                        item.getTaxes().clear();
                    }

                    BigDecimal totalTaxRate = BigDecimal.ZERO;
                    if (product.getTaxes() != null) {
                        for (ProductTax pTax : product.getTaxes()) {
                            totalTaxRate = totalTaxRate.add(pTax.getTaxPercentage());
                            
                            // Create snapshot
                            EstimateItemTax snapshot = EstimateItemTax.builder()
                                    .taxName(pTax.getTaxName())
                                    .taxPercentage(pTax.getTaxPercentage())
                                    .estimateItemEntity(item)
                                    .taxAmount(BigDecimal.ZERO) // Will calculate below
                                    .build();
                            item.addTax(snapshot);
                        }
                    }

                    // 2. Calculate row total based on inclusivity
                    BigDecimal lineBaseTotal;
                    BigDecimal lineGrandTotal;
                    
                    if (item.isTaxInclusive()) {
                        lineGrandTotal = item.getQuantity().multiply(item.getRate());
                        lineBaseTotal = lineGrandTotal.divide(BigDecimal.ONE.add(totalTaxRate.divide(new BigDecimal("100"))), 4, java.math.RoundingMode.HALF_UP);
                    } else {
                        lineBaseTotal = item.getQuantity().multiply(item.getRate());
                        lineGrandTotal = lineBaseTotal.multiply(BigDecimal.ONE.add(totalTaxRate.divide(new BigDecimal("100"))));
                    }

                    // 3. Update snapshot tax amounts
                    for (EstimateItemTax snapshot : item.getTaxes()) {
                        BigDecimal amount = lineBaseTotal.multiply(snapshot.getTaxPercentage().divide(new BigDecimal("100")));
                        snapshot.setTaxAmount(amount);
                    }

                    item.setRowTotal(lineGrandTotal);
                    grandTotal = grandTotal.add(lineGrandTotal);
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
