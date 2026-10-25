package com.bervan.toolsapp;

import com.bervan.common.BervanBaseRepositoryImpl;
import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import com.bervan.logging.JsonLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.bervan.*")
@EnableJpaRepositories(basePackages = "com.bervan.*", repositoryBaseClass = BervanBaseRepositoryImpl.class)
@EntityScan(basePackages = "com.bervan.*")
@EnableMethodSecurity
@EnableAsync
public class Application extends SpringBootServletInitializer {
    private final UserRepository userRepository;
    private final JsonLogger logger = JsonLogger.getLogger(Application.class, "my-tools-app");

    public Application(UserRepository userRepository) {
        this.userRepository = userRepository;

        //comment for new backup
        if (userRepository.findByUsername("COMMON_USER").isEmpty()) {
            User save = new User();
            save.setUsername("COMMON_USER");
            save.setRole("ROLE_USER");
            save.setLockedAccount(true);
            userRepository.save(save);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
