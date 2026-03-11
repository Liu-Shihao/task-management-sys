package com.finblock.tms.config;

import com.finblock.tms.modules.auth.ApiTokenAuthenticationFilter;
import com.finblock.tms.modules.auth.ApiTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ApiTokenService apiTokenService,
            AppProperties appProperties
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new ApiTokenAuthenticationFilter(apiTokenService, appProperties.auth().headerName()),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
