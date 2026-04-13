package com.mariasorganics.billing.repository;

import com.mariasorganics.billing.model.DocumentConfiguration;
import com.mariasorganics.billing.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentConfigurationRepository extends JpaRepository<DocumentConfiguration, Long> {
    Optional<DocumentConfiguration> findByDocumentType(DocumentType documentType);
}
