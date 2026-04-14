package com.mariasorganics.billing.config;

import com.mariasorganics.billing.dto.SettingsFormDto;
import com.mariasorganics.billing.model.Buyer;
import com.mariasorganics.billing.model.Product;
import com.mariasorganics.billing.model.Role;
import com.mariasorganics.billing.model.User;
import com.mariasorganics.billing.repository.BuyerRepository;
import com.mariasorganics.billing.repository.CompanyProfileRepository;
import com.mariasorganics.billing.repository.ProductRepository;
import com.mariasorganics.billing.repository.UserRepository;
import com.mariasorganics.billing.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final SettingsService settingsService;
    private final BuyerRepository buyerRepository;
    private final ProductRepository productRepository;
    private final CompanyProfileRepository profileRepo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Running DevDataSeeder to inject dummy data...");

        seedSecurity();

        if (profileRepo.count() == 0) {
            SettingsFormDto settings = new SettingsFormDto();
            settings.setCompanyName("Maria's Organics");
            settings.setBillingAddress("123 Eco Farm Road\nGreen Valley, KL 682001");
            settings.setGstin("32AAAAAAA0000Z1");
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
            Product p1 = Product.builder()
                    .title("Fresh Oyster Mushrooms (250g)")
                    .supplyRate(new BigDecimal("120.00"))
                    .mrp(new BigDecimal("150.00"))
                    .uom("250g Box")
                    .isActive(true)
                    .build();

            Product p2 = Product.builder()
                    .title("Button Mushrooms (500g)")
                    .supplyRate(new BigDecimal("180.00"))
                    .mrp(new BigDecimal("220.00"))
                    .uom("500g Box")
                    .isActive(true)
                    .build();

            productRepository.saveAll(Arrays.asList(p1, p2));
            log.info("Seeded Products.");
        }

        if (buyerRepository.count() == 0) {
            Buyer b1 = Buyer.builder()
                    .name("Fresh Mart Supermarket")
                    .billingAddress("45 Main Street, Ernakulam")
                    .shippingAddress("45 Main Street, Ernakulam")
                    .gstin("32BBBBBB0000Z2")
                    .phone("+91 9876543210")
                    .isActive(true)
                    .build();

            Buyer b2 = Buyer.builder()
                    .name("Organic Harvest Cafe")
                    .billingAddress("12 MG Road, Kochi")
                    .shippingAddress("12 MG Road, Kochi")
                    .phone("+91 8765432109")
                    .isActive(true)
                    .build();

            buyerRepository.saveAll(Arrays.asList(b1, b2));
            log.info("Seeded Buyers.");
        }
        
        log.info("DevDataSeeder execution completed.");
    }

    private void seedSecurity() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .isActive(true)
                    .build();

            User employee = User.builder()
                    .username("employee")
                    .password(passwordEncoder.encode("emp123"))
                    .role(Role.ROLE_EMPLOYEE)
                    .isActive(true)
                    .build();

            userRepository.saveAll(Arrays.asList(admin, employee));
            log.info("Seeded Admin and Employee users.");
        }
    }
}
