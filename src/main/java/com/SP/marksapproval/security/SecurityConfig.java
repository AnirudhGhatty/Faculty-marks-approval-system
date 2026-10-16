
package com.SP.marksapproval.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new LegacyPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                .requestMatchers(
                    "/student-dashboard",
                    "/student-results",
                    "/student-results/**"
                ).hasRole("STUDENT")

                .requestMatchers(
                    "/faculty-dashboard",
                    "/marks-entry",
                    "/marks-entry/**",
                    "/marks/**",
                    "/ncp-marks/**"
                ).hasRole("FACULTY")

                .requestMatchers(
                    "/admin-dashboard",
                    "/admin/**",
                    "/students/**",
                    "/faculty/**",
                    "/subjects/**",
                    "/faculty-subject/**"
                ).hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    Authentication authentication =
                            org.springframework.security.core.context
                                .SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                    String contextPath = request.getContextPath();
                    String destination = "/login";

                    if (authentication != null
                            && authentication.isAuthenticated()) {

                        var authorities = authentication.getAuthorities();

                        if (authorities.stream().anyMatch(
                                a -> a.getAuthority().equals("ROLE_STUDENT"))) {
                            destination = "/student-dashboard";

                        } else if (authorities.stream().anyMatch(
                                a -> a.getAuthority().equals("ROLE_FACULTY"))) {
                            destination = "/faculty-dashboard";

                        } else if (authorities.stream().anyMatch(
                                a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                            destination = "/admin-dashboard";
                        }
                    }

                    response.sendRedirect(contextPath + destination);
                })
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .failureUrl("/login?error")
                .successHandler((request, response, authentication) -> {
                    var authorities = authentication.getAuthorities();

                    if (authorities.stream().anyMatch(
                            a -> a.getAuthority().equals("ROLE_STUDENT"))) {
                        response.sendRedirect(
                                request.getContextPath() + "/student-dashboard");

                    } else if (authorities.stream().anyMatch(
                            a -> a.getAuthority().equals("ROLE_FACULTY"))) {
                        response.sendRedirect(
                                request.getContextPath() + "/faculty-dashboard");

                    } else if (authorities.stream().anyMatch(
                            a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect(
                                request.getContextPath() + "/admin-dashboard");

                    } else {
                        response.sendRedirect(
                                request.getContextPath() + "/login?error");
                    }
                })
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}
