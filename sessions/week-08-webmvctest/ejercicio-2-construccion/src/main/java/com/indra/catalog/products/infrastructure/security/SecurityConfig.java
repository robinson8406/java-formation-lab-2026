package com.indra.catalog.products.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String editorUsername;
    private final String editorPassword;
    private final String adminUsername;
    private final String adminPassword;

    public SecurityConfig(
            @Value("${app.security.users.editor.username:editor}") String editorUsername,
            @Value("${app.security.users.editor.password:editor123}") String editorPassword,
            @Value("${app.security.users.admin.username:admin}") String adminUsername,
            @Value("${app.security.users.admin.password:admin123}") String adminPassword) {
        this.editorUsername = editorUsername;
        this.editorPassword = editorPassword;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails editor = User.builder()
                .passwordEncoder(passwordEncoder::encode)
                .username(editorUsername)
                .password(editorPassword)
                .roles("EDITOR")
                .build();

        UserDetails admin = User.builder()
                .passwordEncoder(passwordEncoder::encode)
                .username(adminUsername)
                .password(adminPassword)
                .roles("EDITOR", "ADMIN")
                .build();

        return new InMemoryUserDetailsManager(editor, admin);
    }
}
