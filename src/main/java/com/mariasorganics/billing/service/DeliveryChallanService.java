package com.mariasorganics.billing.service;

import com.mariasorganics.billing.model.*;
import com.mariasorganics.billing.repository.DocumentConfigurationRepository;
import com.mariasorganics.billing.repository.DeliveryChallanRepository;
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
public class DeliveryChallanService {
    private final DeliveryChallanRepository deliveryChallanRepository;
    private final DocumentConfigurationRepository docConfigRepo;

    public List<DeliveryChallan> getAllDeliveryChallans() {
        return deliveryChallanRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public DeliveryChallan getDeliveryChallanById(Long id) {
        return deliveryChallanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid delivery challan ID: " + id));
    }

    @Transactional
    public DeliveryChallan saveDeliveryChallan(DeliveryChallan deliveryChallan) {
        if (deliveryChallan.getId() == null && (deliveryChallan.getChallanNumber() == null || deliveryChallan.getChallanNumber().isEmpty())) {
            deliveryChallan.setChallanNumber(generateChallanNumber(deliveryChallan.getBuyerEntity(), deliveryChallan.getChallanDate()));
        }

        // If the delivery challan is cancelled, update the Document # to denote that
        if (deliveryChallan.getStatus() == DeliveryChallanStatus.CANCELLED && deliveryChallan.getChallanNumber() != null && !deliveryChallan.getChallanNumber().endsWith("-CANC")) {
            deliveryChallan.setChallanNumber(deliveryChallan.getChallanNumber() + "-CANC");
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

    private String generateChallanNumber(Buyer buyer, LocalDate date) {
        DocumentConfiguration config = docConfigRepo.findByDocumentType(DocumentType.DELIVERY_CHALLAN)
                .orElse(new DocumentConfiguration());
        String globalPrefix = config.getDocumentPrefix();
        if (globalPrefix == null || globalPrefix.isEmpty()) {
            globalPrefix = "DC";
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
                ? deliveryChallanRepository.countByBuyerEntityAndChallanDateBetweenAndStatusNot(buyer, startOfMonth, endOfMonth, DeliveryChallanStatus.CANCELLED) + 1 
                : deliveryChallanRepository.countByChallanDateBetweenAndStatusNot(startOfMonth, endOfMonth, DeliveryChallanStatus.CANCELLED) + 1;
        
        int month = docDate.getMonthValue(); // 1-12
        int year = docDate.getYear() % 100; // last 2 digits

        return String.format("%s %s-%d%02d%02d", customerPrefix, globalPrefix, count, month, year);
    }
}
