package com.mariasorganics.billing.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class SettingsFormDto {
    private Long profileId;
    private String companyName;
    private String billingAddress;
    private String gstin;
    private String logoFilePath;
    private String signatureBase64;

    private List<ContactDetailDto> contacts = new ArrayList<>();
    private List<RegistrationDetailDto> registrations = new ArrayList<>();

    private String estimatePrefix;
    private String estimateTerms;
    private String estimateFooter;

    private String creditNotePrefix;
    private String creditNoteTerms;
    private String creditNoteFooter;
}
