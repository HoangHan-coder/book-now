package vn.edu.fpt.booknow.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.edu.fpt.booknow.conponents.HttpCookieOAuth2AuthorizationRequest;
import vn.edu.fpt.booknow.conponents.JwtAuthenticationFilter;
import vn.edu.fpt.booknow.conponents.OAuth2LoginSuccessHandler;
import vn.edu.fpt.booknow.services.customer.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2LoginSuccessHandler successHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequest cookieRepo;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // Stateless – No Security Session
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/authen/verifiedOtp","/authen/login", "/authen/registerEmail","/authen/otp","/authen/registerForm","/book-now/staff/bookings/update/*","/checkin/start","/book-now/checkin/page/**").permitAll()
                        .anyRequest().authenticated()
                ).addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(authz -> authz
                                .authorizationRequestRepository(cookieRepo)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(successHandler)
                ).exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/book-now/authen/login");
                        })
                );

        return http.build();
    }
}
