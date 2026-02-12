package org.example.g2bplatform.config;

import org.example.g2bplatform.entity.User;
import org.example.g2bplatform.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class SuperAdminInitializer implements ApplicationRunner {

    private static final String SUPER_ADMIN_USERNAME = "super_admin";
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private final UserRepository userRepository;

    public SuperAdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        userRepository.findByUsername(SUPER_ADMIN_USERNAME).ifPresent(user -> {
            boolean changed = false;
            if (!ROLE_SUPER_ADMIN.equals(user.getRole())) {
                user.setRole(ROLE_SUPER_ADMIN);
                changed = true;
            }
            if (user.getApproved() == null || !user.getApproved()) {
                user.setApproved(true);
                changed = true;
            }
            if (changed) userRepository.save(user);
        });
    }
}
