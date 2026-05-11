package com.bervan.toolsapp.security;

import com.bervan.streamingapp.tv.TvTokenAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationProvider otpAuthenticationProvider;
    private final TvTokenAuthenticationFilter tvTokenAuthenticationFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            CustomAuthenticationProvider customAuthenticationProvider,
            TvTokenAuthenticationFilter tvTokenAuthenticationFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.otpAuthenticationProvider = customAuthenticationProvider;
        this.tvTokenAuthenticationFilter = tvTokenAuthenticationFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            CustomAuthenticationProvider otpProvider
    ) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(otpProvider);
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (Boolean.parseBoolean(System.getProperty("server.ssl.enabled", "false"))) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http.authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/login", "/pocket/**",
                            "/language-learning/**", "/products/**", "/api/tv/pair/**", "/ws/remote-control")
                            .permitAll();
                    auth.requestMatchers("/line-awesome/**", "/static/**", "/images/**", "/player.html").permitAll();
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers("/api/config").permitAll();
                    auth.requestMatchers("/api/**", "/storage/**", "/file-storage-app/**").authenticated();
                })
                .formLogin(form -> {
                    form.loginPage("/login").permitAll();
                    form.defaultSuccessUrl("/generate-otp");
                    form.successForwardUrl("/generate-otp");
                })
                .authenticationProvider(otpAuthenticationProvider)
                .logout(logout -> {
                    logout.logoutUrl("/logout");
                    logout.logoutSuccessUrl("/login");
                });

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(tvTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.cors().configurationSource(request -> {
                    CorsConfiguration cors = new CorsConfiguration();
                    cors.setAllowedOriginPatterns(List.of("*"));
                    cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    cors.setAllowedHeaders(List.of("*"));
                    return cors;
                })
                .and()
                .csrf().disable();

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        );

        return http.build();
    }
}
