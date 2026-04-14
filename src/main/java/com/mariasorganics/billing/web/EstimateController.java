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
@RequestMapping("/estimates")
@RequiredArgsConstructor
public class EstimateController {
    private final EstimateService estimateService;
    private final BuyerService buyerService;
    private final ProductService productService;
    private final PdfGenerationService pdfGenerationService;
    private final TemplateEngine templateEngine;
    private final SettingsService settingsService;

    @GetMapping
    public String listEstimates(Model model) {
        model.addAttribute("estimates", estimateService.getAllEstimates());
        return "estimates-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Estimate estimate = new Estimate();
        estimate.setEstimateDate(LocalDate.now());
        estimate.getItems().add(new EstimateItem());
        
        model.addAttribute("estimate", estimate);
        model.addAttribute("buyers", buyerService.getActiveBuyers());
        model.addAttribute("products", productService.getActiveProducts());
        return "estimate-form";
    }

    @PostMapping("/save")
    public String saveEstimate(@Valid @ModelAttribute("estimate") Estimate estimate, 
                               BindingResult result, 
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("buyers", buyerService.getActiveBuyers());
            model.addAttribute("products", productService.getActiveProducts());
            return "estimate-form";
        }
        
        estimate.getItems().removeIf(item -> item.getProductEntity() == null || item.getProductEntity().getId() == null);
        
        estimateService.saveEstimate(estimate);
        redirectAttributes.addFlashAttribute("successMessage", "Estimate saved successfully.");
        return "redirect:/estimates";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Estimate estimate = estimateService.getEstimateById(id);
        if (estimate.getItems().isEmpty()) {
            estimate.getItems().add(new EstimateItem());
        }
        
        model.addAttribute("estimate", estimate);
        model.addAttribute("buyers", buyerService.getActiveBuyers());
        model.addAttribute("products", productService.getActiveProducts());
        return "estimate-form";
    }

    @PostMapping("/{id}/cancel")
    public String cancelEstimate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Estimate estimate = estimateService.getEstimateById(id);
        estimate.setStatus(EstimateStatus.CANCELLED);
        estimateService.saveEstimate(estimate);
        redirectAttributes.addFlashAttribute("successMessage", "Estimate cancelled successfully.");
        return "redirect:/estimates";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id, HttpServletRequest request) {
        Estimate estimate = estimateService.getEstimateById(id);
        
        Context context = new Context();
        context.setVariable("estimate", estimate);
        
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
        
        String htmlContent = templateEngine.process("pdf/estimate-print", context);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        
        byte[] pdfBytes = pdfGenerationService.generatePdfFromHtml(htmlContent, baseUrl);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + estimate.getEstimateNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
