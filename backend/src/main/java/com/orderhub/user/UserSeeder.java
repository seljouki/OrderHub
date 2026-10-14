package com.orderhub.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the demo ADMIN and STAFF accounts on startup if they do not exist.
 * Disable with {@code SEED_USERS=false} in real deployments.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
class UserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties props;

    UserSeeder(UserRepository users, PasswordEncoder passwordEncoder, SeedProperties props) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createIfMissing(props.adminEmail(), props.adminPassword(), "Ada Admin", Role.ADMIN);
        createIfMissing(props.staffEmail(), props.staffPassword(), "Sam Staff", Role.STAFF);
    }

    private void createIfMissing(String email, String password, String name, Role role) {
        if (!users.existsByEmailIgnoreCase(email)) {
            users.save(new User(email.toLowerCase(), passwordEncoder.encode(password), name, role));
            log.info("Seeded {} user {}", role, email);
        }
    }
}
