package com.mariasorganics.billing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class CreditNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String creditNoteNumber;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    @ToString.Exclude
    private Buyer buyerEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_id", nullable = true)
    @ToString.Exclude
    private Estimate linkedEstimateEntity;

    @Column(length = 255)
    private String reasonText;

    @NotNull
    private BigDecimal totalCredit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private CreditNoteStatus status = CreditNoteStatus.DRAFT;

    @OneToMany(mappedBy = "creditNoteEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<CreditNoteItem> items = new ArrayList<>();

    public void addItem(CreditNoteItem item) {
        items.add(item);
        item.setCreditNoteEntity(this);
    }
}
