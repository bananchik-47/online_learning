package com.ithub.online_learning.config;

import com.ithub.online_learning.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] API_WRITE_PATHS = {
            "/courses/**",
            "/modules/**",
            "/lessons/**",
            "/assignments/**"
    };

    private static final String[] API_READ_PATHS = {
            "/courses/**",
            "/modules/**",
            "/lessons/**",
            "/assignments/**"
    };

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
                                                   DaoAuthenticationProvider authenticationProvider,
                                                   ContentNegotiationStrategy contentNegotiationStrategy) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/",
                                "/login",
                                "/register",
                                "/css/**"
                        ).permitAll()
                        .requestMatchers("/app/admin/**").hasRole("ADMIN")
                        .requestMatchers("/app/**").hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers(HttpMethod.POST, API_WRITE_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, API_WRITE_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, API_WRITE_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API_WRITE_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, API_READ_PATHS).hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers("/enrollments/**").hasRole("STUDENT")
                        .requestMatchers("/submissions/**").hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers("/progress/**").hasRole("STUDENT")
                        .requestMatchers("/files/**", "/users/**").authenticated()
                        .anyRequest().authenticated()
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
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(apiCsrfIgnoreMatchers(contentNegotiationStrategy)))
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    private static RequestMatcher[] apiCsrfIgnoreMatchers(ContentNegotiationStrategy contentNegotiationStrategy) {
        List<RequestMatcher> matchers = new ArrayList<>();
        matchers.add(new OrRequestMatcher(
                new MediaTypeRequestMatcher(contentNegotiationStrategy, MediaType.APPLICATION_JSON),
                new MediaTypeRequestMatcher(contentNegotiationStrategy, MediaType.APPLICATION_XML)
        ));
        matchers.add(new AntPathRequestMatcher("/courses/**"));
        matchers.add(new AntPathRequestMatcher("/modules/**"));
        matchers.add(new AntPathRequestMatcher("/lessons/**"));
        matchers.add(new AntPathRequestMatcher("/assignments/**"));
        matchers.add(new AntPathRequestMatcher("/submissions/**"));
        matchers.add(new AntPathRequestMatcher("/enrollments/**"));
        matchers.add(new AntPathRequestMatcher("/progress/**"));
        matchers.add(new AntPathRequestMatcher("/files/**"));
        matchers.add(new AntPathRequestMatcher("/users/**"));
        return matchers.toArray(RequestMatcher[]::new);
    }
}
