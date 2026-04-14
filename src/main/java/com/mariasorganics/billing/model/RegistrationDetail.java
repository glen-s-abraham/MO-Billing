package com.mariasorganics.billing.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class RegistrationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CompanyProfile companyProfileEntity;

    private String licenseName;
    private String licenseNumber;
}
