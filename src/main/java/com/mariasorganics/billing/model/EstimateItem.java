package com.mariasorganics.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
public class EstimateItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_id")
    @JsonIgnore
    @ToString.Exclude
    private Estimate estimateEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product productEntity;

    @NotNull
    private BigDecimal quantity = BigDecimal.ZERO;

    @NotNull
    private BigDecimal rate = BigDecimal.ZERO;

    @NotNull
    private BigDecimal rowTotal = BigDecimal.ZERO;
}
