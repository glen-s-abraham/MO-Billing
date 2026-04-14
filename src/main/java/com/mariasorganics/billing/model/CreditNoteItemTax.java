package com.mariasorganics.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_credit_note_item_taxes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditNoteItemTax {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_note_item_id")
    @JsonIgnore
    @ToString.Exclude
    private CreditNoteItem creditNoteItemEntity;

    @NotBlank
    private String taxName;

    @NotNull
    private BigDecimal taxPercentage;

    @NotNull
    private BigDecimal taxAmount;
}
