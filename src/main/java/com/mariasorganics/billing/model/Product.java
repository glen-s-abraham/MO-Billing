package com.mariasorganics.billing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 100)
    private String title;

    @NotBlank
    @Column(length = 20)
    private String uom;

    @NotNull
    private BigDecimal supplyRate;

    @NotNull
    private BigDecimal mrp;

    @Column(nullable = false)
    private Boolean isActive = true;
}
