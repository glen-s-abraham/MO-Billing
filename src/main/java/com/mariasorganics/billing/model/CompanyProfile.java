package com.mariasorganics.billing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bill_company_profiles")
@Data
@NoArgsConstructor
public class CompanyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 100)
    private String companyName;

    @Lob
    private String billingAddress;

    @Column(length = 50)
    private String gstin;

    private String logoFilePath;

    @Lob
    private String signatureBase64;
}
