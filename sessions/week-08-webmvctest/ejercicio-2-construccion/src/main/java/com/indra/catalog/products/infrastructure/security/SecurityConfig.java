package com.indra.catalog.products.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Credenciales externalizadas vía variables de entorno (ver {@code application.properties}),
 * igual que en week-07. {@code @WebMvcTest} no aplica esta configuración salvo que se importe
 * explícitamente, porque Spring Security trae su propia autoconfiguración por defecto.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${app.security.editor-password}") String editorPassword,
            @Value("${app.security.admin-password}") String adminPassword) {
        UserDetails editor = User.withUsername("editor")
                .password(passwordEncoder.encode(editorPassword))
                .roles("EDITOR")
                .build();
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode(adminPassword))
                .roles("EDITOR", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(editor, admin);
    }
}
