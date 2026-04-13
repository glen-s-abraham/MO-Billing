package com.mariasorganics.billing.repository;

import com.mariasorganics.billing.model.CreditNoteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditNoteItemRepository extends JpaRepository<CreditNoteItem, Long> {
}
