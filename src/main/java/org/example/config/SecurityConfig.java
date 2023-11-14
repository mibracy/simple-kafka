package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER_WHEN_DOWNGRADE;

import javax.servlet.http.HttpServletRequest;


@Configuration
public class SecurityConfig {

    private static final String[] AUTH_DENYLIST = {
            "/h2-console/**", "/h2-console/login.do?**",
    };
    private static final String[] AUTH_ALLOWLIST = {
             "/websocket", "/sockjs/**","/sql/**","/api/**","/topic/**",
             "/css/**", "/js/**", "/webjars/**", "/oauth/**", "/error/**"
    };

    private static final String[] CSRF_ALLOWLIST = {
            "/websocket", "/sockjs/**","/sql/**","/api/**", "/topic/**",
            "/h2-console/**",
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
                                referrer.policy(NO_REFERRER_WHEN_DOWNGRADE)).frameOptions().sameOrigin())
                .oauth2Login(withDefaults()); // comment out to secure DB completely from access
        return http.build();
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