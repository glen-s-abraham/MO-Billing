package com.mariasorganics.billing.web;

import com.mariasorganics.billing.dto.SettingsFormDto;
import com.mariasorganics.billing.model.*;
import com.mariasorganics.billing.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;

@Controller
@RequestMapping("/delivery-challans")
@RequiredArgsConstructor
public class DeliveryChallanController {
    private final DeliveryChallanService deliveryChallanService;
    private final BuyerService buyerService;
    private final ProductService productService;
    private final PdfGenerationService pdfGenerationService;
    private final TemplateEngine templateEngine;
    private final SettingsService settingsService;

    @GetMapping
    public String listDeliveryChallans(Model model) {
        model.addAttribute("deliveryChallans", deliveryChallanService.getAllDeliveryChallans());
        return "delivery-challans-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        DeliveryChallan challan = new DeliveryChallan();
        challan.setChallanDate(LocalDate.now());
        challan.getItems().add(new DeliveryChallanItem());
        
        model.addAttribute("deliveryChallan", challan);
        model.addAttribute("buyers", buyerService.getActiveBuyers());
        model.addAttribute("products", productService.getActiveProducts());
        return "delivery-challan-form";
    }

    @PostMapping("/save")
    public String saveDeliveryChallan(@Valid @ModelAttribute("deliveryChallan") DeliveryChallan deliveryChallan, 
                                     BindingResult result, 
                                     Model model, 
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("buyers", buyerService.getActiveBuyers());
            model.addAttribute("products", productService.getActiveProducts());
            return "delivery-challan-form";
        }
        
        deliveryChallan.getItems().removeIf(item -> item.getProductEntity() == null || item.getProductEntity().getId() == null);
        
        deliveryChallanService.saveDeliveryChallan(deliveryChallan);
        redirectAttributes.addFlashAttribute("successMessage", "Delivery Challan saved successfully.");
        return "redirect:/delivery-challans";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        DeliveryChallan challan = deliveryChallanService.getDeliveryChallanById(id);
        if (challan.getItems().isEmpty()) {
            challan.getItems().add(new DeliveryChallanItem());
        }
        
        model.addAttribute("deliveryChallan", challan);
        model.addAttribute("buyers", buyerService.getActiveBuyers());
        model.addAttribute("products", productService.getActiveProducts());
        return "delivery-challan-form";
    }

    @PostMapping("/{id}/cancel")
    public String cancelDeliveryChallan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        DeliveryChallan challan = deliveryChallanService.getDeliveryChallanById(id);
        challan.setStatus(DeliveryChallanStatus.CANCELLED);
        deliveryChallanService.saveDeliveryChallan(challan);
        redirectAttributes.addFlashAttribute("successMessage", "Delivery Challan cancelled successfully.");
        return "redirect:/delivery-challans";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id, HttpServletRequest request) {
        DeliveryChallan challan = deliveryChallanService.getDeliveryChallanById(id);
        
        Context context = new Context();
        context.setVariable("deliveryChallan", challan);
        
        SettingsFormDto settings = settingsService.getSettings();
        if (settings.getLogoFilePath() != null && settings.getLogoFilePath().startsWith("/uploads/")) {
            try {
                java.nio.file.Path imagePath = java.nio.file.Paths.get("." + settings.getLogoFilePath());
                byte[] imageBytes = java.nio.file.Files.readAllBytes(imagePath);
                String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
                String extension = settings.getLogoFilePath().toLowerCase().endsWith(".png") ? "png" : "jpeg";
                settings.setLogoFilePath("data:image/" + extension + ";base64," + base64Image);
            } catch (Exception e) {
                System.err.println("Failed to load logo for PDF rendering: " + e.getMessage());
            }
        }
        context.setVariable("settings", settings);
        
        String htmlContent = templateEngine.process("pdf/delivery-challan-print", context);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        
        byte[] pdfBytes = pdfGenerationService.generatePdfFromHtml(htmlContent, baseUrl);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + challan.getChallanNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
