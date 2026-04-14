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
@Table(name = "bill_estimates")
@Data
@NoArgsConstructor
public class Estimate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String estimateNumber;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate estimateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    @ToString.Exclude
    private Buyer buyerEntity;

    @NotNull
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private EstimateStatus status = EstimateStatus.DRAFT;

    @OneToMany(mappedBy = "estimateEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<EstimateItem> items = new ArrayList<>();

    public void addItem(EstimateItem item) {
        items.add(item);
        item.setEstimateEntity(this);
    }
}
