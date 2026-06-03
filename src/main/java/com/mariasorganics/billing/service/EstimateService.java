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
            estimate.setEstimateNumber(generateEstimateNumber(estimate.getBuyerEntity(), estimate.getEstimateDate()));
        }

        // If the estimate is cancelled, update the Document # to denote that
        if (estimate.getStatus() == EstimateStatus.CANCELLED && estimate.getEstimateNumber() != null && !estimate.getEstimateNumber().endsWith("-CANC")) {
            String baseCancel = estimate.getEstimateNumber() + "-CANC";
            String cancelNumber = baseCancel;
            int cancelSuffix = 1;
            while (estimateRepository.existsByEstimateNumber(cancelNumber)) {
                cancelNumber = baseCancel + "-" + cancelSuffix;
                cancelSuffix++;
            }
            estimate.setEstimateNumber(cancelNumber);
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

    private String generateEstimateNumber(Buyer buyer, LocalDate date) {
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

        LocalDate docDate = (date != null) ? date : LocalDate.now();
        LocalDate startOfMonth = docDate.withDayOfMonth(1);
        LocalDate endOfMonth = docDate.withDayOfMonth(docDate.lengthOfMonth());

        long count = (buyer != null) 
                ? estimateRepository.countByBuyerEntityAndEstimateDateBetweenAndStatusNot(buyer, startOfMonth, endOfMonth, EstimateStatus.CANCELLED) + 1 
                : estimateRepository.countByEstimateDateBetweenAndStatusNot(startOfMonth, endOfMonth, EstimateStatus.CANCELLED) + 1;
        
        int month = docDate.getMonthValue(); // 1-12
        int year = docDate.getYear() % 100; // last 2 digits

        String estimateNumber;
        do {
            estimateNumber = String.format("%s %s-%d%02d%02d", customerPrefix, globalPrefix, count, month, year);
            if (!estimateRepository.existsByEstimateNumber(estimateNumber)) {
                break;
            }
            count++;
        } while (true);

        return estimateNumber;
    }
}
