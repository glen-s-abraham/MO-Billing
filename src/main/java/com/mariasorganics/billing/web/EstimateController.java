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
        var allEstimates = estimateService.getAllEstimates();
        model.addAttribute("activeEstimates", allEstimates.stream().filter(e -> e.getStatus() != EstimateStatus.CANCELLED).toList());
        model.addAttribute("cancelledEstimates", allEstimates.stream().filter(e -> e.getStatus() == EstimateStatus.CANCELLED).toList());
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
    @GetMapping("/manual/new")
    public String showManualCreateForm(Model model) {
        model.addAttribute("buyers", buyerService.getActiveBuyers());
        model.addAttribute("products", productService.getActiveProducts());
        return "manual-estimate-form";
    }

    @PostMapping("/manual/generate")
    public String generateManualEstimates(
            @RequestParam("buyerId") Long buyerId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "productIds", required = false) java.util.List<Long> productIds,
            RedirectAttributes redirectAttributes) {
        
        Buyer buyer = buyerService.getBuyerById(buyerId);
        
        if (quantity == null || quantity <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quantity must be greater than 0");
            return "redirect:/estimates/manual/new";
        }
        
        String bookletId = "B-" + System.currentTimeMillis();
        
        for (int i = 0; i < quantity; i++) {
            Estimate estimate = new Estimate();
            estimate.setBuyerEntity(buyer);
            estimate.setEstimateDate(LocalDate.now());
            estimate.setIsManual(true);
            estimate.setBookletId(bookletId);
            estimate.setTotalAmount(java.math.BigDecimal.ZERO);
            
            if (productIds != null && !productIds.isEmpty()) {
                for (Long productId : productIds) {
                    if (productId == null) continue;
                    Product product = productService.getProductById(productId);
                    EstimateItem item = new EstimateItem();
                    item.setProductEntity(product);
                    item.setQuantity(java.math.BigDecimal.ZERO);
                    item.setRate(java.math.BigDecimal.ZERO);
                    item.setMrp(product.getMrp() != null ? product.getMrp() : java.math.BigDecimal.ZERO);
                    item.setRowTotal(java.math.BigDecimal.ZERO);
                    estimate.addItem(item);
                }
            }
            
            estimateService.saveEstimate(estimate);
        }
        
        redirectAttributes.addFlashAttribute("successMessage", quantity + " manual invoices generated successfully. Booklet ID: " + bookletId);
        return "redirect:/estimates/manual/booklets";
    }

    @GetMapping("/manual/booklets")
    public String listBooklets(Model model) {
        java.util.List<String> bookletIds = estimateService.getDistinctBookletIds();
        java.util.List<java.util.Map<String, Object>> booklets = new java.util.ArrayList<>();
        for (String id : bookletIds) {
            java.util.List<Estimate> estimates = estimateService.getEstimatesByBookletId(id);
            if (!estimates.isEmpty()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("bookletId", id);
                map.put("pageCount", estimates.size());
                map.put("buyerName", estimates.get(0).getBuyerEntity() != null ? estimates.get(0).getBuyerEntity().getName() : "Unknown");
                map.put("date", estimates.get(0).getEstimateDate());
                booklets.add(map);
            }
        }
        model.addAttribute("booklets", booklets);
        return "booklets-list";
    }

    @GetMapping("/manual/booklets/{bookletId}/pdf")
    public ResponseEntity<byte[]> downloadBookletPdf(@PathVariable String bookletId, HttpServletRequest request) {
        java.util.List<Estimate> estimates = estimateService.getEstimatesByBookletId(bookletId);
        if (estimates.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Context context = new Context();
        context.setVariable("estimates", estimates);
        
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
        
        String htmlContent = templateEngine.process("pdf/booklet-print", context);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        
        byte[] pdfBytes = pdfGenerationService.generatePdfFromHtml(htmlContent, baseUrl);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Booklet-" + bookletId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
