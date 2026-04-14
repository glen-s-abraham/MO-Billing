package com.mariasorganics.billing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "bill_contact_details")
@Data
@NoArgsConstructor
public class ContactDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_profile_id")
    @JsonIgnore
    private CompanyProfile companyProfileEntity;

    @Enumerated(EnumType.STRING)
    private ContactType contactType;

    @NotBlank
    private String contactValue;
}
