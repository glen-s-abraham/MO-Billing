package com.mariasorganics.billing.repository;

import com.mariasorganics.billing.model.ContactDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactDetailRepository extends JpaRepository<ContactDetail, Long> {
}
