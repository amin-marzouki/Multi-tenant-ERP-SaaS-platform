package com.orbiterp.common;

import com.orbiterp.security.AuthenticatedUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/** Fills createdAt, createdBy, updatedAt and updatedBy on every {@link BaseEntity}. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(AuthenticatedUser.current().map(AuthenticatedUser::username).orElse("system"));
    }
}
