package vn.edu.fpt.booknow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.fpt.booknow.services.CustomUserDetailsService;
import vn.edu.fpt.booknow.services.JWTService;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ApplicationContext context;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        
        // Skip JWT validation for public endpoints
        if (requestPath.contains("/auth/login") || requestPath.contains("/admin/login") || 
            requestPath.contains("/register") || requestPath.contains("/public") ||
            requestPath.contains("/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        Cookie[] cookies = request.getCookies();
        String authHeader = null;
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("Access_token")) {
                    authHeader = c.getValue();
                }
            }
        }
        String token = null;
        String username = null;

        // get token and username from header
        if (authHeader != null){
            token = authHeader;
            username = jwtService.extractUserName(token);
        }

        // validate token with username
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // get user from database through UserDetailsService
                UserDetails userDetails =  context.getBean(CustomUserDetailsService.class).loadUserByUsername(username);
                // validate token
                if (userDetails != null && jwtService.validateToken(token, userDetails)) {
                    setAuthentication(userDetails, request);
                }
            } catch (Exception e) {
                // Log error but don't block the request
                System.err.println("JWT Filter error: " + e.getMessage());
            }
        }
        filterChain.doFilter(request,response);
    }

    public void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
        // add info request into authentication token
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // add authentication token into security context holder
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
