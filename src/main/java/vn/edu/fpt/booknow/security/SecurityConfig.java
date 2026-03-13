package vn.edu.fpt.booknow.security;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration authConfig
//    ) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }

    // FilterChain (lọc request)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/assets/**","/admin/create/**", "/admin/detail/**","/admin/list","/admin/edit/**", "/staff/update/**", "/admin/dashboard", "/admin/dashboard/export/**", "/admin/room/delete/**").permitAll() // permitAll = cho phép công cộng
                        //.requestMatchers("/artist/list").hasRole("ADMIN")
                        .anyRequest().authenticated() // anyReq = tất cả các request còn lại, buộc phải xác thực
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .defaultSuccessUrl("/artist/list")
                        .failureUrl("/login?message=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                ).exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {

                            response.sendRedirect("/book-now/admin/list");

                        })
                )
                .build();
    }
}

