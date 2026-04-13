package com.mariasorganics.billing.web;

import com.mariasorganics.billing.model.Buyer;
import com.mariasorganics.billing.service.BuyerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/buyers")
@RequiredArgsConstructor
public class BuyerController {
    private final BuyerService buyerService;

    @GetMapping
    public String listBuyers(Model model) {
        model.addAttribute("buyers", buyerService.getAllBuyers());
        return "buyers-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("buyer", new Buyer());
        return "buyer-form";
    }

    @PostMapping("/save")
    public String saveBuyer(@Valid @ModelAttribute("buyer") Buyer buyer, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "buyer-form";
        }
        buyerService.saveBuyer(buyer);
        redirectAttributes.addFlashAttribute("successMessage", "Buyer saved successfully.");
        return "redirect:/buyers";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("buyer", buyerService.getBuyerById(id));
        return "buyer-form";
    }
}
