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
@Table(name = "bill_delivery_challans")
@Data
@NoArgsConstructor
public class DeliveryChallan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String challanNumber;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate challanDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    @ToString.Exclude
    private Buyer buyerEntity;

    @NotNull
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private DeliveryChallanStatus status = DeliveryChallanStatus.DRAFT;

    @OneToMany(mappedBy = "deliveryChallanEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<DeliveryChallanItem> items = new ArrayList<>();

    public void addItem(DeliveryChallanItem item) {
        items.add(item);
        item.setDeliveryChallanEntity(this);
    }
}
