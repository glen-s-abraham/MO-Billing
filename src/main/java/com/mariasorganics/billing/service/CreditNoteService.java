package com.mariasorganics.billing.service;

import com.mariasorganics.billing.model.*;
import com.mariasorganics.billing.repository.CreditNoteRepository;
import com.mariasorganics.billing.repository.DocumentConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditNoteService {
    private final CreditNoteRepository creditNoteRepository;
    private final DocumentConfigurationRepository docConfigRepo;

    public List<CreditNote> getAllCreditNotes() {
        return creditNoteRepository.findAll();
    }

    public CreditNote getCreditNoteById(Long id) {
        return creditNoteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid credit note ID: " + id));
    }

    @Transactional
    public CreditNote saveCreditNote(CreditNote creditNote) {
        if (creditNote.getId() == null && (creditNote.getCreditNoteNumber() == null || creditNote.getCreditNoteNumber().isEmpty())) {
            creditNote.setCreditNoteNumber(generateCreditNoteNumber());
        }

        BigDecimal grandTotal = BigDecimal.ZERO;
        if (creditNote.getItems() != null) {
            for (CreditNoteItem item : creditNote.getItems()) {
                if (item.getProductEntity() != null && item.getReturnQuantity() != null && item.getRate() != null) {
                    Product product = item.getProductEntity();
                    
                    // Clear existing snapshots
                    if (item.getTaxes() != null) {
                        item.getTaxes().clear();
                    }

                    BigDecimal lineGrandTotal;
                    BigDecimal lineBaseTotal;

                    if (item.isTaxEnabled()) {
                        BigDecimal totalTaxRate = BigDecimal.ZERO;
                        if (product.getTaxes() != null) {
                            for (ProductTax pTax : product.getTaxes()) {
                                totalTaxRate = totalTaxRate.add(pTax.getTaxPercentage());
                                
                                CreditNoteItemTax snapshot = CreditNoteItemTax.builder()
                                        .taxName(pTax.getTaxName())
                                        .taxPercentage(pTax.getTaxPercentage())
                                        .creditNoteItemEntity(item)
                                        .taxAmount(BigDecimal.ZERO)
                                        .build();
                                item.addTax(snapshot);
                            }
                        }

                        if (item.isTaxInclusive()) {
                            // Inclusive: Rate includes tax
                            lineGrandTotal = item.getReturnQuantity().multiply(item.getRate());
                            lineBaseTotal = lineGrandTotal.divide(BigDecimal.ONE.add(totalTaxRate.divide(new BigDecimal("100"))), 4, java.math.RoundingMode.HALF_UP);
                        } else {
                            // Exclusive: Rate is base, add tax on top
                            lineBaseTotal = item.getReturnQuantity().multiply(item.getRate());
                            lineGrandTotal = lineBaseTotal.multiply(BigDecimal.ONE.add(totalTaxRate.divide(new BigDecimal("100"))));
                        }

                        for (CreditNoteItemTax snapshot : item.getTaxes()) {
                            BigDecimal amount = lineBaseTotal.multiply(snapshot.getTaxPercentage().divide(new BigDecimal("100")));
                            snapshot.setTaxAmount(amount);
                        }
                    } else {
                        // Tax Disabled
                        lineBaseTotal = item.getReturnQuantity().multiply(item.getRate());
                        lineGrandTotal = lineBaseTotal;
                    }

                    item.setRowTotal(lineGrandTotal);
                    grandTotal = grandTotal.add(lineGrandTotal);
                }
                item.setCreditNoteEntity(creditNote);
            }
        }
        creditNote.setTotalCredit(grandTotal);

        return creditNoteRepository.save(creditNote);
    }

    private String generateCreditNoteNumber() {
        DocumentConfiguration config = docConfigRepo.findByDocumentType(DocumentType.CREDIT_NOTE)
                .orElse(new DocumentConfiguration());
        String prefix = config.getDocumentPrefix();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "CRN-";
        }

        long count = creditNoteRepository.count() + 1;
        return prefix + String.format("%04d", count);
    }
}
