package com.mariasorganics.billing.cli;

import com.mariasorganics.billing.model.Role;
import com.mariasorganics.billing.model.User;
import com.mariasorganics.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Scanner;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperUserCreator implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (Arrays.asList(args).contains("--create-superuser")) {
            createSuperUser();
            System.exit(0);
        }
    }

    private void createSuperUser() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n--- Super User Creation Utility ---");
            
            System.out.print("Enter Username: ");
            String username = scanner.nextLine().trim();
            
            if (userRepository.findByUsername(username).isPresent()) {
                System.err.println("Error: User already exists!");
                return;
            }

            System.out.print("Enter Password: ");
            String password = scanner.nextLine().trim();

            System.out.print("Assign Role (ADMIN/EMPLOYEE) [ADMIN]: ");
            String roleInput = scanner.nextLine().trim().toUpperCase();
            Role role = Role.ROLE_ADMIN;
            if ("EMPLOYEE".equals(roleInput)) {
                role = Role.ROLE_EMPLOYEE;
            }

            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .isActive(true)
                    .build();

            userRepository.save(user);
            System.out.println("\nSUCCESS: User '" + username + "' created with role " + role);
        }
    }
}
