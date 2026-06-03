package com.mariasorganics.billing.repository;

import com.mariasorganics.billing.model.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {
    long countByBuyerEntityAndStatusNot(com.mariasorganics.billing.model.Buyer buyer, com.mariasorganics.billing.model.CreditNoteStatus status);
    long countByStatusNot(com.mariasorganics.billing.model.CreditNoteStatus status);
    long countByBuyerEntityAndIssueDateBetweenAndStatusNot(com.mariasorganics.billing.model.Buyer buyer, java.time.LocalDate startDate, java.time.LocalDate endDate, com.mariasorganics.billing.model.CreditNoteStatus status);
    long countByIssueDateBetweenAndStatusNot(java.time.LocalDate startDate, java.time.LocalDate endDate, com.mariasorganics.billing.model.CreditNoteStatus status);
    boolean existsByCreditNoteNumber(String creditNoteNumber);
}
