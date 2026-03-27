package vn.edu.fpt.booknow.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.edu.fpt.booknow.conponents.HttpCookieOAuth2AuthorizationRequest;
import vn.edu.fpt.booknow.conponents.OAuth2LoginSuccessHandler;
import vn.edu.fpt.booknow.services.CustomUserDetailsService;
import vn.edu.fpt.booknow.services.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JWTFilter jwtFilter;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequest cookieRepo;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2LoginSuccessHandler successHandler;

    @Autowired
    private CustomUserDetailsService detailsService;

    @Autowired
    private RecaptchaFilter recaptchaFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain StaffAccountFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/admin/**", "/staff/**", "/housekeeping/**","/checkin/**", "/payments/**")
                .authenticationProvider(staffAuthProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/auth/login",
                                "/auth/logout", "/public/**",
                                "/home", "/search",
                                "/detail/**", "/pay/**", "/assets/**",
                                "/admin/dashboard",
                                "/forgot-password", "/verify-otp",
                                "/resend-otp", "/reset-password",
                                "/staff/offline-checkin",
                                "/staff/bookings/update-status",
                                "/404", "/error", "/approve","reject", "/payments/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/housekeeping/**", "/payments/**").hasRole("HOUSEKEEPING")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(recaptchaFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain CustomerFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(customerAuthProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login",
                                "/auth/logout", "/register",
                                "/public/**", "/pay/**",
                                "/home", "/search", "/detail/**",
                                "/forgot-password",
                                "/assets/**",
                                "/verify-otp",
                                "/resend-otp",
                                "/oauth2/**",
                                "/reset-password",
                                "/404", "/error", "/auth/verifiedOtp",
                                "/auth/registerEmail", "/auth/otp",
                                "/auth/registerForm",
                                "/checkin/start",
                                "/checkin/booking-code",
                                "/checkin/page",
                                "/checkin/success",
                                "/checkin/fail",
                                "/ws/**", "/checkin/approve", "/checkin/reject", "/payments/**")
                        .permitAll()
                        .anyRequest().hasRole("CUSTOMER"))

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(authz -> authz
                                .authorizationRequestRepository(cookieRepo))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(successHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println(
                                    "throw exceptions tại security config ............................");
                            response.sendRedirect("/book-now/auth/login");
                        }))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(recaptchaFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider customerAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(detailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationProvider staffAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(detailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager manager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
