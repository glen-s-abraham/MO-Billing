package com.mariasorganics.billing.service;

import com.mariasorganics.billing.dto.ContactDetailDto;
import com.mariasorganics.billing.dto.SettingsFormDto;
import com.mariasorganics.billing.model.CompanyProfile;
import com.mariasorganics.billing.model.ContactDetail;
import com.mariasorganics.billing.model.DocumentConfiguration;
import com.mariasorganics.billing.model.DocumentType;
import com.mariasorganics.billing.repository.CompanyProfileRepository;
import com.mariasorganics.billing.repository.ContactDetailRepository;
import com.mariasorganics.billing.repository.DocumentConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final CompanyProfileRepository companyProfileRepo;
    private final ContactDetailRepository contactDetailRepo;
    private final DocumentConfigurationRepository docConfigRepo;
    private final FileStorageService fileStorageService;

    public SettingsFormDto getSettings() {
        SettingsFormDto dto = new SettingsFormDto();

        CompanyProfile profile = companyProfileRepo.findAll().stream().findFirst().orElse(new CompanyProfile());
        dto.setProfileId(profile.getId());
        dto.setCompanyName(profile.getCompanyName());
        dto.setBillingAddress(profile.getBillingAddress());
        dto.setLogoFilePath(profile.getLogoFilePath());
        dto.setSignatureBase64(profile.getSignatureBase64());

        List<ContactDetail> contacts = contactDetailRepo.findAll();
        List<ContactDetailDto> contactDtos = contacts.stream().map(c -> {
            ContactDetailDto cd = new ContactDetailDto();
            cd.setId(c.getId());
            cd.setContactType(c.getContactType());
            cd.setContactValue(c.getContactValue());
            return cd;
        }).collect(Collectors.toList());
        dto.setContacts(contactDtos);

        DocumentConfiguration estimate = docConfigRepo.findByDocumentType(DocumentType.ESTIMATE)
                .orElse(new DocumentConfiguration());
        dto.setEstimatePrefix(estimate.getDocumentPrefix());
        dto.setEstimateTerms(estimate.getTermsAndConditions());
        dto.setEstimateFooter(estimate.getFooterNotes());

        DocumentConfiguration creditNote = docConfigRepo.findByDocumentType(DocumentType.CREDIT_NOTE)
                .orElse(new DocumentConfiguration());
        dto.setCreditNotePrefix(creditNote.getDocumentPrefix());
        dto.setCreditNoteTerms(creditNote.getTermsAndConditions());
        dto.setCreditNoteFooter(creditNote.getFooterNotes());

        return dto;
    }

    @Transactional
    public void saveSettings(SettingsFormDto dto, MultipartFile logoFile) {
        CompanyProfile profile = companyProfileRepo.findAll().stream().findFirst().orElse(new CompanyProfile());
        profile.setCompanyName(dto.getCompanyName());
        profile.setBillingAddress(dto.getBillingAddress());
        
        if (dto.getSignatureBase64() != null && !dto.getSignatureBase64().isEmpty()) {
            profile.setSignatureBase64(dto.getSignatureBase64());
        }

        if (logoFile != null && !logoFile.isEmpty()) {
            String newLogoPath = fileStorageService.storeLogoFile(logoFile);
            profile.setLogoFilePath(newLogoPath);
        }
        companyProfileRepo.save(profile);

        contactDetailRepo.deleteAll();
        if (dto.getContacts() != null) {
            for (ContactDetailDto cDto : dto.getContacts()) {
                if(cDto.getContactValue() != null && !cDto.getContactValue().trim().isEmpty()) {
                    ContactDetail detail = new ContactDetail();
                    detail.setCompanyProfileEntity(profile);
                    detail.setContactType(cDto.getContactType());
                    detail.setContactValue(cDto.getContactValue());
                    contactDetailRepo.save(detail);
                }
            }
        }

        saveDocConfig(DocumentType.ESTIMATE, dto.getEstimatePrefix(), dto.getEstimateTerms(), dto.getEstimateFooter());
        saveDocConfig(DocumentType.CREDIT_NOTE, dto.getCreditNotePrefix(), dto.getCreditNoteTerms(), dto.getCreditNoteFooter());
    }

    private void saveDocConfig(DocumentType type, String prefix, String terms, String footer) {
        DocumentConfiguration config = docConfigRepo.findByDocumentType(type).orElse(new DocumentConfiguration());
        config.setDocumentType(type);
        config.setDocumentPrefix(prefix);
        config.setTermsAndConditions(terms);
        config.setFooterNotes(footer);
        docConfigRepo.save(config);
    }

    @Transactional
    public void deleteLogo() {
        CompanyProfile profile = companyProfileRepo.findAll().stream().findFirst().orElse(new CompanyProfile());
        if (profile.getLogoFilePath() != null) {
            fileStorageService.deleteFile(profile.getLogoFilePath());
            profile.setLogoFilePath(null);
            companyProfileRepo.save(profile);
        }
    }

    @Transactional
    public void deleteSignature() {
        CompanyProfile profile = companyProfileRepo.findAll().stream().findFirst().orElse(new CompanyProfile());
        profile.setSignatureBase64(null);
        companyProfileRepo.save(profile);
    }
}
