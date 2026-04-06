package com.smart.rh.config;

import com.smart.rh.entity.Role;
import com.smart.rh.entity.User;
import com.smart.rh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes demo users with correct bcrypt-hashed passwords on MySQL profile startup.
 * Runs AFTER Flyway migrations to fix any incorrect password hashes in migration files.
 */
@Slf4j
@Component
@Profile("mysql")
@RequiredArgsConstructor
public class UserPasswordInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("UserPasswordInitializer: checking and fixing demo user passwords...");
        
        // Define demo users with their passwords
        final var demoUsers = new java.util.LinkedHashMap<String, DemoUser>();
        demoUsers.put("admin", new DemoUser("admin@smartrh.com", "Admin@2024", Role.ROLE_ADMIN));
        demoUsers.put("rhmanager", new DemoUser("rh@smartrh.com", "Rh@2024", Role.ROLE_RH));
        demoUsers.put("alice.dubois", new DemoUser("alice@smartrh.com", "Employee@2024", Role.ROLE_EMPLOYEE));
        demoUsers.put("bob.martin", new DemoUser("bob@smartrh.com", "Employee@2024", Role.ROLE_EMPLOYEE));

        for (var entry : demoUsers.entrySet()) {
            String username = entry.getKey();
            DemoUser demoUser = entry.getValue();

            var user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                User u = user.get();
                // Re-hash password to ensure it's correct
                String newHash = passwordEncoder.encode(demoUser.password);
                u.setPassword(newHash);
                u.setEnabled(true);
                userRepository.save(u);
                log.info("UserPasswordInitializer: updated user '{}' with correct password hash", username);
            } else {
                // Create user if it doesn't exist
                User u = new User();
                u.setUsername(username);
                u.setEmail(demoUser.email);
                u.setPassword(passwordEncoder.encode(demoUser.password));
                u.setRole(demoUser.role);
                u.setEnabled(true);
                userRepository.save(u);
                log.info("UserPasswordInitializer: created user '{}' with password hash", username);
            }
        }

        log.info("UserPasswordInitializer: demo user passwords initialization complete!");
    }

    private static class DemoUser {
        final String email;
        final String password;
        final Role role;

        DemoUser(String email, String password, Role role) {
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }
}
