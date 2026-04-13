package com.mariasorganics.billing.web;

import com.mariasorganics.billing.model.Estimate;
import com.mariasorganics.billing.service.EstimateService;
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
@RequestMapping("/api/estimates")
@RequiredArgsConstructor
public class EstimateRestController {
    private final EstimateService estimateService;

    @GetMapping("/{id}")
    public ResponseEntity<EstimateDto> getEstimate(@PathVariable Long id) {
        Estimate est = estimateService.getEstimateById(id);
        
        List<EstimateItemDto> items = est.getItems().stream()
            .map(item -> new EstimateItemDto(
                item.getProductEntity().getId(),
                item.getProductEntity().getTitle(),
                item.getProductEntity().getUom(),
                item.getQuantity(),
                item.getRate()))
            .collect(Collectors.toList());

        Long buyerId = est.getBuyerEntity() != null ? est.getBuyerEntity().getId() : null;

        EstimateDto dto = new EstimateDto(est.getId(), est.getEstimateNumber(), buyerId, items);
        return ResponseEntity.ok(dto);
    }

    @Data
    @AllArgsConstructor
    static class EstimateDto {
        private Long id;
        private String estimateNumber;
        private Long buyerId;
        private List<EstimateItemDto> items;
    }

    @Data
    @AllArgsConstructor
    static class EstimateItemDto {
        private Long productId;
        private String productTitle;
        private String productUom;
        private BigDecimal quantity;
        private BigDecimal rate;
    }
}
