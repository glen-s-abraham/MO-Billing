package com.mariasorganics.billing.web;

import com.mariasorganics.billing.dto.SettingsFormDto;
import com.mariasorganics.billing.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public String showSettingsForm(Model model) {
        SettingsFormDto dto = settingsService.getSettings();
        model.addAttribute("settingsForm", dto);
        return "settings";
    }

    @PostMapping("/save")
    public String saveSettings(@ModelAttribute("settingsForm") SettingsFormDto settingsForm,
                               @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                               RedirectAttributes redirectAttributes) {
        settingsService.saveSettings(settingsForm, logoFile);
        redirectAttributes.addFlashAttribute("successMessage", "Settings saved successfully.");
        return "redirect:/settings";
    }

    @PostMapping("/delete-logo")
    public String deleteLogo(RedirectAttributes redirectAttributes) {
        settingsService.deleteLogo();
        redirectAttributes.addFlashAttribute("successMessage", "Logo deleted successfully.");
        return "redirect:/settings";
    }

    @PostMapping("/delete-signature")
    public String deleteSignature(RedirectAttributes redirectAttributes) {
        settingsService.deleteSignature();
        redirectAttributes.addFlashAttribute("successMessage", "Signature deleted successfully.");
        return "redirect:/settings";
    }
}
