package com.invision.web.Invision.config;

import com.invision.web.Invision.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF PROTECTION FULLY ENABLED ENFORCEMENT
                // No more ignoring matches for business APIs. Every state-mutating request
                // (POST, PUT, DELETE) now strictly requires a valid CSRF token.
                .csrf(csrf -> csrf.configure(http))

                .authorizeHttpRequests(auth -> auth
                        // Public Assets & Non-authenticated view layers
                        .requestMatchers(
                                "/","/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico",
                                "/login", "/register",
                                "/forgot-password/**",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()

                        // Strict Granular User Management Endpoint Lockdown
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyRole("ADMIN")

                        // Core Resource Domain Rules
                        .requestMatchers("/api/assets/**", "/api/loans/**").authenticated()

                        .anyRequest().authenticated()
                )
                .userDetailsService(customUserDetailsService)
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession)
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(2)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired=true")
                        .sessionRegistry(sessionRegistry()) // 2. Linked Registry to support admin boots
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}