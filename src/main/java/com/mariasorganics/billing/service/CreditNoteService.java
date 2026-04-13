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
                if(item.getReturnQuantity() != null && item.getRate() != null) {
                    BigDecimal rowTotal = item.getReturnQuantity().multiply(item.getRate());
                    item.setRowTotal(rowTotal);
                    grandTotal = grandTotal.add(rowTotal);
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
