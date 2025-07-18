package gr.atc.modapto.config;

import java.util.Arrays;
import java.util.List;

import gr.atc.modapto.filter.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import gr.atc.modapto.security.JwtAuthConverter;
import gr.atc.modapto.security.UnauthorizedEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.cors.domains}")
    private String corsDomainsRaw;

    /**
     * Initialize and Configure Security Filter Chain of HTTP connection
     *
     * @param http       HttpSecurity
     * @param entryPoint UnauthorizedEntryPoint -> To add proper API Response to the
     *                   authorized request
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, UnauthorizedEntryPoint entryPoint)
            throws Exception {
        // Convert JWT Roles with class to Spring Security Roles
        JwtAuthConverter jwtAuthConverter = new JwtAuthConverter();

        // Set Session to Stateless so not to keep any information about the JWT
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configure CORS access
                .cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))
                // Configure CSRF Token - Disable
                .csrf(AbstractHttpConfigurer::disable)
                // Rate Limit Filter
                .addFilterBefore(new RateLimitingFilter(), SecurityContextHolderFilter.class)
                .exceptionHandling(exc -> exc.authenticationEntryPoint(entryPoint))
                // HTTP Requests authorization properties on URLs
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/api/eds/swagger", "/api/eds/swagger-ui/**", "/api/eds/v3/api-docs/**", "/eds/websocket/**").permitAll()
                        .anyRequest().authenticated())
                // JWT Authentication Configuration
                .oauth2ResourceServer(oauth2ResourceServerCustomizer -> oauth2ResourceServerCustomizer
                        .jwt(jwtCustomizer -> jwtCustomizer.jwtAuthenticationConverter(jwtAuthConverter)));
        return http.build();
    }

    /**
     * Settings for CORS
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Split and trim domains
        List<String> corsDomains = Arrays.stream(corsDomainsRaw.split(","))
                .map(String::trim)
                .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsDomains);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(86400L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // JWT Issuer Decoder
    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

}
