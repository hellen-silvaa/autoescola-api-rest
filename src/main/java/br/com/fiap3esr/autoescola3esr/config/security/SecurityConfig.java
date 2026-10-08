package br.com.fiap3esr.autoescola3esr.config.security;

import br.com.fiap3esr.autoescola3esr.config.security.filter.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/login", "/health-check").permitAll()
                                .requestMatchers("/v3/api-docs", "/v3/api-docs.yaml",
                                        "/v3/api-docs/**",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**").permitAll()
                                //Esse é o padrão de mercado para gerenciamento de autorizações
                                /*.requestMatchers(HttpMethod.POST, "/instrutores").hasRole("ADMIN") // Instruções para autorizações individuais
                                .requestMatchers(HttpMethod.GET, "/instrutores").hasAnyRole("ADMIN", "USER")
                                .requestMatchers(HttpMethod.GET, "/instrutores/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/instrutores").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/instrutores/{id}").hasRole("ADMIN")*/
                                //.requestMatchers("/instrutores").hasRole("ADMIN") // Instrução para autorização em grupo
                                .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}