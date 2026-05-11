package com.bervan.toolsapp.security;

import com.bervan.streamingapp.tv.TvTokenAuthenticationFilter;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        HttpSecurity httpSecurity;
        if (Boolean.parseBoolean(System.getProperty("server.ssl.enabled", "false"))) {
            httpSecurity = http.requiresChannel(channel ->
                    channel.anyRequest().requiresSecure());
        } else {
            httpSecurity = http;
        }

        httpSecurity.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
                    authorizationManagerRequestMatcherRegistry.requestMatchers("/login", "/pocket/**",
                            "/language-learning/**", "/products/**", "/api/tv/pair/**", "/ws/remote-control")
                            .permitAll();
                    authorizationManagerRequestMatcherRegistry.requestMatchers("/line-awesome/**", "/static/**", "/images/**", "/player.html").permitAll();
                    authorizationManagerRequestMatcherRegistry.requestMatchers("/api/auth/**").permitAll();
                    authorizationManagerRequestMatcherRegistry.requestMatchers("/api/config").permitAll();
                    authorizationManagerRequestMatcherRegistry.requestMatchers("/api/**", "/storage/**", "/file-storage-app/**").authenticated();
                })
                .formLogin(httpSecurityFormLoginConfigurer -> {
                    httpSecurityFormLoginConfigurer.loginPage("/login").permitAll();
                    httpSecurityFormLoginConfigurer.defaultSuccessUrl("/generate-otp");
                    httpSecurityFormLoginConfigurer.successForwardUrl("/generate-otp");
                })
                .authenticationProvider(otpAuthenticationProvider)
                .logout(httpSecurityLogoutConfigurer -> {
                    httpSecurityLogoutConfigurer.logoutUrl("/logout");
                    httpSecurityLogoutConfigurer.logoutSuccessUrl("/login");
                });


        super.configure(http);

        // JWT filter for React app, TV token filter for TV app.
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

    }
}