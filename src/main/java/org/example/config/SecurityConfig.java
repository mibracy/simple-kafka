package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    private static final String[] AUTH_DENYLIST = {
            "/h2-console/**", "/h2-console/login.do?**"
    };

    // Oauth2 Secured endpoints
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).headers(head -> head
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(AUTH_DENYLIST).fullyAuthenticated()
                        .anyRequest().permitAll())
            .oauth2Login(withDefaults());
        return http.build();
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