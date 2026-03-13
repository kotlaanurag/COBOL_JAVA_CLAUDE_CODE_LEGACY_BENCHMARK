package com.clbs.investment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Replaces COBOL z/OS RACF security integration (SECMGR, CICS transaction security).
 *
 * COBOL security model:
 *  - SECMGR: validates user authorization, resource access control, audit logging
 *  - z/OS RACF: user profiles, resource classes, access lists
 *  - CICS: transaction-level security
 *
 * Java equivalent:
 *  - INQUIRY role  → INQPORT / INQHIST read-only access (replaces CICS read transactions)
 *  - BATCH role    → batch job trigger endpoints (replaces JCL submit authority)
 *  - ADMIN role    → full access including reports (replaces RACF SPECIAL attribute)
 *
 * For production, replace InMemoryUserDetailsManager with LDAP or JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/portfolio/**", "/api/history/**").hasAnyRole("INQUIRY", "ADMIN")
                .requestMatchers("/api/batch/**").hasAnyRole("BATCH", "ADMIN")
                .requestMatchers("/api/reports/**").hasAnyRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        // Replace with LDAP/database-backed UserDetailsService in production
        return new InMemoryUserDetailsManager(
            User.withUsername("inquiry").password(encoder.encode("inquiry123"))
                .roles("INQUIRY").build(),
            User.withUsername("batch").password(encoder.encode("batch123"))
                .roles("BATCH").build(),
            User.withUsername("admin").password(encoder.encode("admin123"))
                .roles("ADMIN").build()
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
