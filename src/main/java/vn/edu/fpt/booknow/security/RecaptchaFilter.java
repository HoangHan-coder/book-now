package vn.edu.fpt.booknow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.fpt.booknow.services.RecaptchaService;

import java.io.IOException;

@Component
public class RecaptchaFilter extends OncePerRequestFilter {

    @Autowired
    private RecaptchaService recaptchaService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        String contextPath = request.getContextPath();
        if ((servletPath.equals("/auth/login") || servletPath.equals("/admin/login"))
                && "POST".equalsIgnoreCase(request.getMethod())) {

            String recaptcha =
                    request.getParameter("g-recaptcha-response");
            if (!recaptchaService.verify(recaptcha)) {
                response.sendRedirect(contextPath + servletPath + "?captchaError");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}