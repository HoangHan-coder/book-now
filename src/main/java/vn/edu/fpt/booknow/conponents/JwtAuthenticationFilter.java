package vn.edu.fpt.booknow.conponents;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter  {

    @Autowired
    private JwtUtils jwtUtils;


    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("doFilterInternal running..........................");
        String token = null;

        if (request.getServletPath().startsWith("/login/oauth2/**")
                || request.getServletPath().startsWith("/login/oauth2/**")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("Access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }



        if (token != null) {
            try {
                String email = jwtUtils.getEmailFromToken(token);
                System.out.println("email: " + email);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email, null, List.of()
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception ex) {

                // ✅ TOKEN SAI → KHÔNG restart OAuth
                SecurityContextHolder.clearContext();

                response.sendRedirect("/book-now/auth/login");
                return;
            }


        }

        filterChain.doFilter(request, response);
    }
}
