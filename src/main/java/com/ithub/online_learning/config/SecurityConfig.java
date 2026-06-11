package com.ithub.online_learning.config;

import com.ithub.online_learning.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/register", "/login").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/courses", "/courses/**")
                        .hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers("/courses/new", "/courses/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/courses", "/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/courses", "/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/courses", "/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses", "/courses/**").hasRole("ADMIN")
                        .requestMatchers("/enrollments/**").hasRole("STUDENT")
                        .requestMatchers("/lessons/**").hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers("/submissions/**").hasRole("STUDENT")
                        .requestMatchers("/progress/**").hasRole("STUDENT")
                        .requestMatchers("/users/**").authenticated()
                        .requestMatchers("/modules/**").authenticated()
                        .requestMatchers("/assignments/**").authenticated()
                        .requestMatchers("/files/**").authenticated()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/register",
                                "/courses/**",
                                "/modules/**",
                                "/lessons/**",
                                "/assignments/**",
                                "/submissions/**",
                                "/enrollments/**",
                                "/progress/**",
                                "/files/**",
                                "/users/**"
                        )
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
