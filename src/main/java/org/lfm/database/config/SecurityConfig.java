package org.lfm.database.config;

import org.lfm.database.user.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig
{
    public static final String WEBHOOK_PATH = "/api/forms/webhook";

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy()
    {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(Role.ADMIN.name()).implies(Role.STAFF.name())
                .role(Role.STAFF.name()).implies(Role.VIEWER.name())
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(WEBHOOK_PATH))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(WEBHOOK_PATH).permitAll()
                .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/people", true)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll());

        return http.build();
    }
}
