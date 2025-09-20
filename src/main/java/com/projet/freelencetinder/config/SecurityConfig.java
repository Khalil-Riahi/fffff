package com.projet.freelencetinder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.projet.freelencetinder.filter.JwtAuthenticationFilter;
import com.projet.freelencetinder.servcie.CustomUserDetailsService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(CustomUserDetailsService uds, JwtAuthenticationFilter jwtFilter) {
        this.userDetailsService = uds;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // --- PUBLIC POUR TESTS ---
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/v1/paymee/webhook").permitAll()
                .requestMatchers("/api/files/upload").permitAll()
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                // >> AJOUTS IMPORTANTS <<
                .requestMatchers("/api/swipes/**").permitAll()     // tests swipe
                .requestMatchers("/ws/**").permitAll()             // SockJS /ws & /ws/info
                .requestMatchers("/api/missions/**").permitAll()   // lecture missions
                .requestMatchers("/api/utilisateurs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/conversations/init").authenticated()
                .requestMatchers("/api/v1/paiement/**").permitAll()


                // --- RESTE PROTÉGÉ ---
                .anyRequest().permitAll()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}