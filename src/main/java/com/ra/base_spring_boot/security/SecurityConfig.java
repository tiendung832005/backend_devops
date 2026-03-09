package com.ra.base_spring_boot.security;

import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.security.exception.AccessDenied;
import com.ra.base_spring_boot.security.exception.JwtEntryPoint;
import com.ra.base_spring_boot.security.jwt.JwtTokenFilter;
import com.ra.base_spring_boot.security.principle.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final MyUserDetailsService userDetailsService;
        private final JwtEntryPoint jwtEntryPoint;
        private final AccessDenied accessDenied;
        private final JwtTokenFilter jwtTokenFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                return http
                                .cors(cf -> cf.configurationSource(request -> {
                                        CorsConfiguration config = new CorsConfiguration();
                                        config.setAllowedOrigins(List.of("http://localhost:3000"));
                                        config.setAllowedMethods(List.of("*"));
                                        config.setAllowedHeaders(List.of("*"));
                                        config.setExposedHeaders(List.of("*"));
                                        config.setAllowCredentials(true);
                                        return config;
                                }))
                                .csrf(AbstractHttpConfigurer::disable)

                                // AUTHORIZATION BLOCK — phải nằm trong authorizeHttpRequests
                                .authorizeHttpRequests(auth -> auth

                                                // WRITER ROUTES - ROLE_WRITER can create/update, ROLE_ADMIN can view
                                                .requestMatchers("/api/v1/writer/**")
                                                .hasAnyAuthority(
                                                                RoleName.ROLE_WRITER.toString(),
                                                                RoleName.ROLE_ADMIN.toString())
                                                .requestMatchers("/api/v1/auth/**", "/ws/**").permitAll()
                                                .requestMatchers("api/v1/otp/**").permitAll()
                                                // User bookmarks
                                                .requestMatchers("/api/v1/bookmarks/**")
                                                .hasAnyAuthority(
                                                                RoleName.ROLE_WRITER.toString(),
                                                                RoleName.ROLE_ADMIN.toString())
                                                // NOTIFICATION
                                                .requestMatchers("/api/v1/notification", "/api/v1/notification/**")
                                                .hasAnyAuthority(
                                                                RoleName.ROLE_READER.toString(),
                                                                RoleName.ROLE_WRITER.toString(),
                                                                RoleName.ROLE_ADMIN.toString())
                                                // Specific rules for comments filter should be BEFORE general admin
                                                // routes
                                                .requestMatchers("/api/v1/admin/comments/advanced-filter")
                                                .hasAnyAuthority(
                                                                RoleName.ROLE_ADMIN.toString(),
                                                                RoleName.ROLE_WRITER.toString())

                                                // CATEGORY ROUTES
                                                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**")
                                                .permitAll() // Allow everyone to view categories

                                                .requestMatchers(HttpMethod.POST, "/api/v1/categories/**")
                                                .hasAnyAuthority(RoleName.ROLE_ADMIN.toString(),
                                                                RoleName.ROLE_WRITER.toString())

                                                .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**")
                                                .hasAnyAuthority(RoleName.ROLE_ADMIN.toString(),
                                                                RoleName.ROLE_WRITER.toString())
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**")
                                                .hasAnyAuthority(RoleName.ROLE_ADMIN.toString())
                                                // ADMIN ROUTES
                                                .requestMatchers("/api/v1/admin/**")
                                                .hasAuthority(RoleName.ROLE_ADMIN.toString())
                                                // ACTIVITY LOGS ROUTES - Admin only
                                                .requestMatchers("/api/v1/activity-logs/**")
                                                .hasAuthority(RoleName.ROLE_ADMIN.toString())

                                                // USER ROUTES
                                                .requestMatchers("/api/v1/users/**")
                                                .hasAnyAuthority(
                                                                RoleName.ROLE_READER.toString(),
                                                                RoleName.ROLE_WRITER.toString(),
                                                                RoleName.ROLE_ADMIN.toString())
                                                .requestMatchers(HttpMethod.GET, "/users/export")
                                                .hasRole("ADMIN")

                                                // Default: all remaining requests require authentication
                                                .anyRequest().authenticated())

                                .oauth2Login(oauth -> oauth.disable())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(jwtEntryPoint)
                                                .accessDeniedHandler(accessDenied))

                                .authenticationProvider(authenticationProvider())

                                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)

                                .build();

        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setPasswordEncoder(passwordEncoder());
                provider.setUserDetailsService(userDetailsService);
                return provider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
                return auth.getAuthenticationManager();
        }
}
