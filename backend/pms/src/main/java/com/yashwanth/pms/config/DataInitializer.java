package com.yashwanth.pms.config;

import com.yashwanth.pms.user.domain.Role;
import com.yashwanth.pms.user.domain.User;
import com.yashwanth.pms.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.count() > 0) return ;

    userRepository.save(
        new User("Admin", "admin@example.com",
            passwordEncoder.encode("password"),
            Role.ADMIN)
    );

    userRepository.save(
        new User("Leader", "leader@example.com",
            passwordEncoder.encode("password"),
            Role.PROJECT_LEADER)
    );

    userRepository.save(
        new User("Member", "member@example.com",
            passwordEncoder.encode("password"),
            Role.TEAM_MEMBER)
    );
    }
}
