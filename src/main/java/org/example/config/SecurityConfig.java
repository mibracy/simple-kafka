package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER_WHEN_DOWNGRADE;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Configuration
public class SecurityConfig {

    //    https://manage.auth0.com/
    @Value("${okta.oauth2.issuer}")
    String issuer;

    @Value("${okta.oauth2.client-id}")
    String clientId;

    private static final String[] AUTH_DENYLIST = {
            "/h2-console/**", "/h2-console/login.do?**",
    };
    private static final String[] AUTH_ALLOWLIST = {
             "/websocket", "/sockjs/**","/sql/**","/api/**","/topic/**",
             "/css/**", "/js/**", "/webjars/**", "/oauth/**", "/error/**"
    };

    private static final String[] CSRF_ALLOWLIST = {
            "/websocket", "/sockjs/**","/sql/**","/api/**", "/topic/**",
            "/h2-console/**", "/landing"
    };

    // Oauth2 Secured endpoints
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().ignoringAntMatchers(CSRF_ALLOWLIST)
                .and()
                .authorizeRequests()
                .antMatchers(AUTH_DENYLIST).fullyAuthenticated()
                .antMatchers(AUTH_ALLOWLIST).permitAll()
                .and()
                .headers(headers ->
                        headers.referrerPolicy(referrer ->
                                referrer.policy(NO_REFERRER_WHEN_DOWNGRADE))
                                .frameOptions().sameOrigin()
                                .xssProtection().block(true)
                ).oauth2Login(okta -> okta
                        .defaultSuccessUrl("/home")
                        .redirectionEndpoint( redirect ->
                                redirect.baseUri("/login/oauth2/code/okta")))
                .logout(logout -> logout
                        .addLogoutHandler(logoutHandler()));;
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
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return new ResponseEntity<>("No Auth in Header!", HttpStatus.FORBIDDEN);
        } else if (!toke.equals(header.substring(7))) {
            return new ResponseEntity<>("Invalid Auth in Header!", HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>("Yippee Ki-Yay!", HttpStatus.OK);
    }
}