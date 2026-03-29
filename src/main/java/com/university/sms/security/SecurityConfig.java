package com.university.sms.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true, 
    jsr250Enabled = true, 
    prePostEnabled = true
)
@Slf4j
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Using explicit AntPathRequestMatcher to resolve compilation "not applicable for arguments" error
                .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/favicon.ico")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.png")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.gif")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.svg")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.jpg")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.html")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.css")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.js")).permitAll()
                
                // Auth endpoints
                .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                
                // Public GET endpoints (specified with HttpMethod and Pattern)
                .requestMatchers(new AntPathRequestMatcher("/api/courses/available", HttpMethod.GET.name())).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/departments", HttpMethod.GET.name())).permitAll()
                
                // All other requests must be authenticated
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        log.info("Security configuration initialized with explicit AntPathRequestMatchers");
        return http.build();
    }
}
