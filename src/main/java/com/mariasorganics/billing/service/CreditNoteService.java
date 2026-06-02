package com.mariasorganics.billing.service;

import com.mariasorganics.billing.model.*;
import com.mariasorganics.billing.repository.CreditNoteRepository;
import com.mariasorganics.billing.repository.DocumentConfigurationRepository;
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
public class CreditNoteService {
    private final CreditNoteRepository creditNoteRepository;
    private final DocumentConfigurationRepository docConfigRepo;

    public List<CreditNote> getAllCreditNotes() {
        return creditNoteRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public CreditNote getCreditNoteById(Long id) {
        return creditNoteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid credit note ID: " + id));
    }

    @Transactional
    public CreditNote saveCreditNote(CreditNote creditNote) {
        if (creditNote.getId() == null && (creditNote.getCreditNoteNumber() == null || creditNote.getCreditNoteNumber().isEmpty())) {
            creditNote.setCreditNoteNumber(generateCreditNoteNumber(creditNote.getBuyerEntity()));
        }

        // If the credit note is cancelled (VOID), update the Document # to denote that
        if (creditNote.getStatus() == CreditNoteStatus.VOID && creditNote.getCreditNoteNumber() != null && !creditNote.getCreditNoteNumber().endsWith("-CANC")) {
            creditNote.setCreditNoteNumber(creditNote.getCreditNoteNumber() + "-CANC");
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

    private String generateCreditNoteNumber(Buyer buyer) {
        DocumentConfiguration config = docConfigRepo.findByDocumentType(DocumentType.CREDIT_NOTE)
                .orElse(new DocumentConfiguration());
        String globalPrefix = config.getDocumentPrefix();
        if (globalPrefix == null || globalPrefix.isEmpty()) {
            globalPrefix = "CRN";
        } else {
            globalPrefix = globalPrefix.replace("-", "").trim();
        }

        String customerPrefix = buyer != null && buyer.getInvoicePrefix() != null && !buyer.getInvoicePrefix().trim().isEmpty() 
                ? buyer.getInvoicePrefix().trim() 
                : (buyer != null && buyer.getName() != null ? buyer.getName().trim() : "CUST");

        long count = (buyer != null) 
                ? creditNoteRepository.countByBuyerEntityAndStatusNot(buyer, CreditNoteStatus.VOID) + 1 
                : creditNoteRepository.countByStatusNot(CreditNoteStatus.VOID) + 1;
        
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue(); // 1-12 without zero padding
        int year = now.getYear() % 100; // last 2 digits

        return String.format("%s %s-%d%d%d", customerPrefix, globalPrefix, count, month, year);
    }
}
