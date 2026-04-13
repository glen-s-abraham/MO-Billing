package com.mariasorganics.billing.config;

import com.mariasorganics.billing.dto.SettingsFormDto;
import com.mariasorganics.billing.model.Buyer;
import com.mariasorganics.billing.model.ContactDetail;
import com.mariasorganics.billing.model.DocumentConfiguration;
import com.mariasorganics.billing.model.DocumentType;
import com.mariasorganics.billing.model.Product;
import com.mariasorganics.billing.repository.BuyerRepository;
import com.mariasorganics.billing.repository.CompanyProfileRepository;
import com.mariasorganics.billing.repository.ContactDetailRepository;
import com.mariasorganics.billing.repository.DocumentConfigurationRepository;
import com.mariasorganics.billing.repository.ProductRepository;
import com.mariasorganics.billing.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final SettingsService settingsService;
    private final BuyerRepository buyerRepository;
    private final ProductRepository productRepository;
    private final DocumentConfigurationRepository docConfigRepo;
    private final CompanyProfileRepository profileRepo;
    private final ContactDetailRepository contactDetRepo;

    @Override
    public void run(String... args) throws Exception {
        log.info("Running DevDataSeeder to inject dummy data...");

        if (profileRepo.count() == 0) {
            SettingsFormDto settings = new SettingsFormDto();
            settings.setCompanyName("Maria's Organics");
            settings.setBillingAddress("123 Eco Farm Road\nGreen Valley, KL 682001");
            settings.setEstimatePrefix("EST-");
            settings.setEstimateTerms("Payment due within 15 days.\nGoods once sold will not be taken back.");
            settings.setEstimateFooter("Bank: State Bank of India\nA/C: 1234567890\nIFSC: SBIN0001234");
            
            settings.setCreditNotePrefix("CRN-");
            settings.setCreditNoteTerms("Credit is valid for 6 months from issue date.");
            settings.setCreditNoteFooter("For any queries, contact support.");
            
            settingsService.saveSettings(settings, null);
            log.info("Seeded Company Profile and Document Configs.");
        }

        if (productRepository.count() == 0) {
            Product p1 = new Product();
            p1.setSku("MO-OY-250");
            p1.setTitle("Fresh Oyster Mushrooms (250g)");
            p1.setDescription("Premium quality freshly harvested oyster mushrooms");
            p1.setSupplyRate(new BigDecimal("120.00"));
            p1.setMrp(new BigDecimal("150.00"));
            p1.setUom("250g Box");
            productRepository.save(p1);

            Product p2 = new Product();
            p2.setSku("MO-BM-500");
            p2.setTitle("Button Mushrooms (500g)");
            p2.setDescription("Large white button mushrooms");
            p2.setSupplyRate(new BigDecimal("180.00"));
            p2.setMrp(new BigDecimal("220.00"));
            p2.setUom("500g Box");
            productRepository.save(p2);
            log.info("Seeded Products.");
        }

        if (buyerRepository.count() == 0) {
            Buyer b1 = new Buyer();
            b1.setName("Fresh Mart Supermarket");
            b1.setBillingAddress("45 Main Street, Ernakulam");
            b1.setShippingAddress("45 Main Street, Ernakulam");
            b1.setTaxId("GSTIN23456789");
            b1.setContactNumber("+91 9876543210");
            buyerRepository.save(b1);

            Buyer b2 = new Buyer();
            b2.setName("Organic Harvest Cafe");
            b2.setBillingAddress("12 MG Road, Kochi");
            b2.setShippingAddress("12 MG Road, Kochi");
            b2.setContactNumber("+91 8765432109");
            buyerRepository.save(b2);
            log.info("Seeded Buyers.");
        }
        
        log.info("DevDataSeeder execution completed.");
    }
}
