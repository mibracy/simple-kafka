package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    //    https://manage.auth0.com/
    @Value("${okta.oauth2.issuer}")
    String issuer;

    @Value("${okta.oauth2.client-id}")
    String clientId;

    private static final String[] AUTH_SECURED = {
            "/h2-console/**", "/h2-console/login.do?**",
    };

    // Oauth2 Secured endpoints
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).headers(head -> head
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(AUTH_SECURED).fullyAuthenticated()
                        .anyRequest().permitAll())
                .oauth2Login(okta -> okta
                        .defaultSuccessUrl("/home")
                        .redirectionEndpoint( redirect ->
                                redirect.baseUri("/login/oauth2/code/okta")))
                .logout(logout -> logout
                        .addLogoutHandler(logoutHandler()));
        return http.build();
    }

    private LogoutHandler logoutHandler() {
        return (request, response, authentication) -> {
            try {
                String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/home";
                response.sendRedirect(issuer + "v2/logout?client_id=" + clientId + "&returnTo=" + baseUrl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    // Simple Bearer Token Authorization
    public static ResponseEntity<String> authHeaderCheck(HttpServletRequest request, String toke) {
        var header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return new ResponseEntity<>("No Auth in Header!", HttpStatus.FORBIDDEN);
        } else if (!toke.equals(header.substring(7))) {
            return new ResponseEntity<>("Invalid Auth in Header!", HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>("Yippee Ki-Yay!", HttpStatus.OK);
    }
}