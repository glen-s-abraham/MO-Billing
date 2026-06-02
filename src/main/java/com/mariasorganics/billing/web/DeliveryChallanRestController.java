package com.mariasorganics.billing.web;

import com.mariasorganics.billing.model.DeliveryChallan;
import com.mariasorganics.billing.service.DeliveryChallanService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/delivery-challans")
@RequiredArgsConstructor
public class DeliveryChallanRestController {
    private final DeliveryChallanService deliveryChallanService;

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryChallanDto> getDeliveryChallan(@PathVariable Long id) {
        DeliveryChallan challan = deliveryChallanService.getDeliveryChallanById(id);
        
        List<DeliveryChallanItemDto> items = challan.getItems().stream()
            .map(item -> new DeliveryChallanItemDto(
                item.getProductEntity().getId(),
                item.getProductEntity().getTitle(),
                item.getProductEntity().getUom(),
                item.getQuantity(),
                item.getRate()))
            .collect(Collectors.toList());

        Long buyerId = challan.getBuyerEntity() != null ? challan.getBuyerEntity().getId() : null;

        DeliveryChallanDto dto = new DeliveryChallanDto(challan.getId(), challan.getChallanNumber(), buyerId, items);
        return ResponseEntity.ok(dto);
    }

    @Data
    @AllArgsConstructor
    static class DeliveryChallanDto {
        private Long id;
        private String challanNumber;
        private Long buyerId;
        private List<DeliveryChallanItemDto> items;
    }

    @Data
    @AllArgsConstructor
    static class DeliveryChallanItemDto {
        private Long productId;
        private String productTitle;
        private String productUom;
        private BigDecimal quantity;
        private BigDecimal rate;
    }
}
