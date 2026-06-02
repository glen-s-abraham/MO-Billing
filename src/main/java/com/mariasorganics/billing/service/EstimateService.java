package com.mariasorganics.billing.service;

import com.mariasorganics.billing.model.*;
import com.mariasorganics.billing.repository.DocumentConfigurationRepository;
import com.mariasorganics.billing.repository.EstimateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstimateService {
    private final EstimateRepository estimateRepository;
    private final DocumentConfigurationRepository docConfigRepo;

    public List<Estimate> getAllEstimates() {
        return estimateRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Estimate getEstimateById(Long id) {
        return estimateRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid estimate ID: " + id));
    }

    @Transactional
    public Estimate saveEstimate(Estimate estimate) {
        if (estimate.getId() == null && (estimate.getEstimateNumber() == null || estimate.getEstimateNumber().isEmpty())) {
            estimate.setEstimateNumber(generateEstimateNumber(estimate.getBuyerEntity()));
        }

        // If the estimate is cancelled, update the Document # to denote that
        if (estimate.getStatus() == EstimateStatus.CANCELLED && estimate.getEstimateNumber() != null && !estimate.getEstimateNumber().endsWith("-CANC")) {
            estimate.setEstimateNumber(estimate.getEstimateNumber() + "-CANC");
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

    private String generateEstimateNumber(Buyer buyer) {
        DocumentConfiguration config = docConfigRepo.findByDocumentType(DocumentType.ESTIMATE)
                .orElse(new DocumentConfiguration());
        String globalPrefix = config.getDocumentPrefix();
        if (globalPrefix == null || globalPrefix.isEmpty()) {
            globalPrefix = "EST";
        } else {
            globalPrefix = globalPrefix.replace("-", "").trim();
        }

        String customerPrefix = buyer != null && buyer.getInvoicePrefix() != null && !buyer.getInvoicePrefix().trim().isEmpty() 
                ? buyer.getInvoicePrefix().trim() 
                : (buyer != null && buyer.getName() != null ? buyer.getName().trim() : "CUST");

        long count = (buyer != null) 
                ? estimateRepository.countByBuyerEntityAndStatusNot(buyer, EstimateStatus.CANCELLED) + 1 
                : estimateRepository.countByStatusNot(EstimateStatus.CANCELLED) + 1;
        
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue(); // 1-12 without zero padding
        int year = now.getYear() % 100; // last 2 digits

        return String.format("%s %s-%d%d%d", customerPrefix, globalPrefix, count, month, year);
    }
}
