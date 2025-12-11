package com.institucion.ticketero.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for easier API interaction, consider enabling for production
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/index.html"),
                                new AntPathRequestMatcher("/api/tickets/**"),
                                new AntPathRequestMatcher("/api/dashboard"),
                                new AntPathRequestMatcher("/api/public-dashboard/**"),
                                new AntPathRequestMatcher("/api/notifications/telegram-bot-username"),
                                new AntPathRequestMatcher("/swagger-ui/**"),
                                new AntPathRequestMatcher("/v3/api-docs/**")
                        ).permitAll()
                        
                        // Admin endpoints
                        .requestMatchers(
                                new AntPathRequestMatcher("/gestion.html"),
                                new AntPathRequestMatcher("/api/executives/**"),
                                new AntPathRequestMatcher("/api/workday/**")
                        ).hasRole("ADMIN")
                        
                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                .formLogin(withDefaults()); // Use default login page
        return http.build();
    }
}
