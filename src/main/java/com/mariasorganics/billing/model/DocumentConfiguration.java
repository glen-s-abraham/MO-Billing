package com.mariasorganics.billing.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bill_document_configs")
@Data
@NoArgsConstructor
public class DocumentConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(length = 10)
    private String documentPrefix;

    @Lob
    private String termsAndConditions;

    @Lob
    private String footerNotes;
}
