package com.mariasorganics.billing.service;

import com.mariasorganics.billing.model.Buyer;
import com.mariasorganics.billing.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuyerService {
    private final BuyerRepository buyerRepository;

    public List<Buyer> getAllBuyers() {
        return buyerRepository.findAll();
    }

    public List<Buyer> getActiveBuyers() {
        return buyerRepository.findByIsActiveTrue();
    }

    public Buyer getBuyerById(Long id) {
        return buyerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid buyer ID: " + id));
    }

    @Transactional
    public Buyer saveBuyer(Buyer buyer) {
        return buyerRepository.save(buyer);
    }
}
