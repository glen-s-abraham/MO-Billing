package com.mariasorganics.billing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Buyer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 100)
    private String name;

    @Lob
    private String billingAddress;

    @Lob
    private String shippingAddress;

    @Column(length = 50)
    private String taxId;

    @Column(length = 100)
    private String contactPerson;

    @Email
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private Boolean isActive = true;
}
