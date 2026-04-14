package com.mariasorganics.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bill_estimate_items")
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
    private BigDecimal mrp = BigDecimal.ZERO;

    @NotNull
    private BigDecimal rate = BigDecimal.ZERO;

    @NotNull
    private BigDecimal rowTotal = BigDecimal.ZERO;

    private boolean isTaxInclusive = false;

    @OneToMany(mappedBy = "estimateItemEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<EstimateItemTax> taxes = new ArrayList<>();

    public void addTax(EstimateItemTax tax) {
        taxes.add(tax);
        tax.setEstimateItemEntity(this);
    }
}
