package vn.edu.fpt.booknow.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import vn.edu.fpt.booknow.conponents.OAuth2LoginSuccessHandler;
import vn.edu.fpt.booknow.services.customer.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth ->
                        auth.requestMatchers("/authen/login", "/authen/registerEmail")
                                .permitAll().anyRequest().authenticated())
                .oauth2Login(oauth ->
                        oauth.loginPage("/authen/login")
                                .userInfoEndpoint(userInfo ->
                                        userInfo.userService(customOAuth2UserService))
                                .successHandler(oAuth2LoginSuccessHandler));

        return http.build();
    }
}
