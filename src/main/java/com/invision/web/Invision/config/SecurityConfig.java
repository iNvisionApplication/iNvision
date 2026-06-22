package com.invision.web.Invision.config;

import com.invision.web.Invision.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
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
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/assets/**",
                                "/api/loans/**",
                                "/api/users/**",
                                "/users/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/forgot-password/**"
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Public Assets & Non-authenticated view layers

                                // Public Assets & Non-authenticated view layers
                                .requestMatchers(
                                        "/", // 💡 ADD THIS LINE: Explicitly opens access to your root index landing page view
                                        "/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico",
                                        "/error", "/error/**",
                                        "/login", "/register",
                                        "/forgot-password/**",
                                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                                ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/**")
                        .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**")
                        .hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users/**")
                        .hasAnyRole("ADMIN")
                        .requestMatchers("/api/assets/**", "/api/loans/**")
                        .authenticated()

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
                        .sessionRegistry(sessionRegistry())
                ).exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, exception) -> {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                })
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