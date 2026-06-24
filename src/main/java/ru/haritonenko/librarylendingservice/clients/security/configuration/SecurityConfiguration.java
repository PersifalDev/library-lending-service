package ru.haritonenko.librarylendingservice.clients.security.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import ru.haritonenko.librarylendingservice.clients.security.custom.authentification.CustomAuthenticationEntryPoint;
import ru.haritonenko.librarylendingservice.clients.security.custom.handler.CustomAccessDeniedHandler;
import ru.haritonenko.librarylendingservice.clients.security.custom.service.CustomClientDetailsService;
import ru.haritonenko.librarylendingservice.clients.security.jwt.filter.JwtTokenFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomClientDetailsService customClientDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenFilter jwtTokenFilter) throws Exception {
        http
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()
                .and()
                .authorizeRequests()
                .antMatchers("/", "/index.html", "/favicon.ico", "/swagger-ui/**", "/swagger-ui.html",
                        "/v3/api-docs/**", "/v3/api-docs", "/v3/api-docs.yaml", "/openapi.yaml",
                        "/h2-console/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/clients").permitAll()
                .antMatchers(HttpMethod.POST, "/api/clients/auth").permitAll()
                .antMatchers(HttpMethod.GET, "/api/clients/debug/auth").permitAll()
                .antMatchers(HttpMethod.GET, "/api/clients/**").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/clients/*").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/books").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/books/*").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/books/**").hasAnyAuthority("ADMIN", "USER")
                .antMatchers(HttpMethod.POST, "/api/lendings").hasAnyAuthority("ADMIN", "USER")
                .antMatchers(HttpMethod.GET, "/api/lendings/**").hasAnyAuthority("ADMIN", "USER")
                .antMatchers(HttpMethod.PATCH, "/api/lendings/*/return").hasAnyAuthority("ADMIN", "USER")
                .antMatchers("/error", "/error/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
                .and()
                .addFilterBefore(jwtTokenFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customClientDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
