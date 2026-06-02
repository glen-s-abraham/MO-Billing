package com.mariasorganics.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_delivery_challan_items")
@Data
@NoArgsConstructor
public class DeliveryChallanItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_challan_id")
    @JsonIgnore
    @ToString.Exclude
    private DeliveryChallan deliveryChallanEntity;

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
}
