package com.mariasorganics.billing.dto;

import com.mariasorganics.billing.model.ContactType;
import lombok.Data;

@Data
public class ContactDetailDto {
    private Long id;
    private ContactType contactType;
    private String contactValue;
}
