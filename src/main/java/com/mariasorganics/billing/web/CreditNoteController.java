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
@RequestMapping("/credit-notes")
@RequiredArgsConstructor
public class CreditNoteController {
    private final CreditNoteService creditNoteService;
    private final EstimateService estimateService;
    private final BuyerService buyerService;
    private final ProductService productService;
    private final PdfGenerationService pdfGenerationService;
    private final TemplateEngine templateEngine;
    private final SettingsService settingsService;

    @GetMapping
    public String listCreditNotes(Model model) {
        model.addAttribute("creditNotes", creditNoteService.getAllCreditNotes());
        return "credit-notes-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        CreditNote creditNote = new CreditNote();
        creditNote.setIssueDate(LocalDate.now());
        creditNote.getItems().add(new CreditNoteItem());
        
        model.addAttribute("creditNote", creditNote);
        model.addAttribute("buyers", buyerService.getActiveBuyers());
        model.addAttribute("estimates", estimateService.getAllEstimates());
        model.addAttribute("products", productService.getActiveProducts());
        return "credit-note-form";
    }

    @PostMapping("/save")
    public String saveCreditNote(@Valid @ModelAttribute("creditNote") CreditNote creditNote, 
                               BindingResult result, 
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("buyers", buyerService.getActiveBuyers());
            model.addAttribute("estimates", estimateService.getAllEstimates());
            model.addAttribute("products", productService.getActiveProducts());
            return "credit-note-form";
        }
        
        creditNote.getItems().removeIf(item -> item.getProductEntity() == null || item.getProductEntity().getId() == null);
        
        if(creditNote.getLinkedEstimateEntity() != null && creditNote.getLinkedEstimateEntity().getId() == null) {
            creditNote.setLinkedEstimateEntity(null);
        }

        creditNoteService.saveCreditNote(creditNote);
        redirectAttributes.addFlashAttribute("successMessage", "Credit Note saved successfully.");
        return "redirect:/credit-notes";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        CreditNote creditNote = creditNoteService.getCreditNoteById(id);
        if (creditNote.getItems().isEmpty()) {
            creditNote.getItems().add(new CreditNoteItem());
        }
        
        model.addAttribute("creditNote", creditNote);
        model.addAttribute("buyers", buyerService.getActiveBuyers());
        model.addAttribute("estimates", estimateService.getAllEstimates());
        model.addAttribute("products", productService.getActiveProducts());
        return "credit-note-form";
    }

    @PostMapping("/{id}/void")
    public String voidCreditNote(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CreditNote creditNote = creditNoteService.getCreditNoteById(id);
        creditNote.setStatus(CreditNoteStatus.VOID);
        creditNoteService.saveCreditNote(creditNote);
        redirectAttributes.addFlashAttribute("successMessage", "Credit Note voided successfully.");
        return "redirect:/credit-notes";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id, HttpServletRequest request) {
        CreditNote creditNote = creditNoteService.getCreditNoteById(id);
        
        Context context = new Context();
        context.setVariable("creditNote", creditNote);
        
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
        
        String htmlContent = templateEngine.process("pdf/credit-note-print", context);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        
        byte[] pdfBytes = pdfGenerationService.generatePdfFromHtml(htmlContent, baseUrl);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + creditNote.getCreditNoteNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
