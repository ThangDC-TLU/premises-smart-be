package com.badmintonhub.premisessmartbe;

import com.badmintonhub.premisessmartbe.entity.Role;
import com.badmintonhub.premisessmartbe.entity.User;
import com.badmintonhub.premisessmartbe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.full-name:Administrator}")
    private String adminFullName;

    @Value("${app.admin.phone:}")
    private String adminPhone;

    @Bean
    CommandLineRunner initAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            users.findByEmail(adminEmail).ifPresentOrElse(
                    u -> System.out.println("[ADMIN-SEED] Admin existed: " + adminEmail),
                    () -> {
                        User admin = new User();
                        admin.setEmail(adminEmail);
                        admin.setPassword(encoder.encode(adminPassword));
                        admin.setFullName(adminFullName);
                        admin.setPhone(adminPhone);
                        admin.setRole(Role.ADMIN);

                        users.save(admin);
                        System.out.println("[ADMIN-SEED] Admin created: " + adminEmail);
                    }
            );
        };
    }
}
