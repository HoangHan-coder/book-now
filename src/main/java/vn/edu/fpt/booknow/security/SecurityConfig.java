package vn.edu.fpt.booknow.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import vn.edu.fpt.booknow.services.customer.CustomerService;
import vn.edu.fpt.booknow.services.staffadmin.StaffAccountService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    StaffAccountService staffAccountService;
    @Autowired
    CustomerService customerService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }


    @Bean
    @Order(1)
    public SecurityFilterChain StaffAccountFilterChain(HttpSecurity http,  AdminAuthenticationProvider adminProvider) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/admin/**")
                .authenticationProvider(staffAuthProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/register", "/public/**", "/home").permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .failureUrl("/admin/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/admin/login")
                        .invalidateHttpSession(true));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain CustomerFilterChain(HttpSecurity http, CustomerAuthencationProvider customerAuthencationProvider) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/**")
                .authenticationProvider(customerAuthProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/register", "/public/**", "/home").permitAll()
                        .anyRequest().hasRole("CUSTOMER")
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/auth/login")
                        .successHandler(oauth2SuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login")
                        .invalidateHttpSession(true));

        return http.build();
    }


//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http,
//                                                       AdminAuthenticationProvider adminAuthenticationProvider,
//                                                       CustomerAuthencationProvider customerAuthencationProvider
//
//    ) throws Exception{
//
//        return http.getSharedObject(AuthenticationManagerBuilder.class)
//                .authenticationProvider(adminAuthenticationProvider)
//                .authenticationProvider(customerAuthencationProvider)
//                .build();
//    }

    @Bean
    public AuthenticationProvider customerAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customerService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationProvider staffAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(staffAccountService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
