package org.lfm.database.config;

import org.lfm.database.user.AppUserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig
{
    @Bean
    public AuditorAware<Long> auditorAware()
    {
        return () ->
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if(authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken)
            {
                return Optional.empty();
            }

            if(authentication.getPrincipal() instanceof AppUserPrincipal principal)
            {
                return Optional.ofNullable(principal.getId());
            }

            return Optional.empty();
        };
    }
}
