package vn.edu.fpt.booknow.conponents;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        boolean isNewUser = (boolean) oAuth2User.getAttribute("isNewUser");

        if (isNewUser) {
            System.out.println("O day");

            response.sendRedirect("/book-now/auth/login");
        } else {
            String email = oAuth2User.getAttribute("email");


            String token = JwtUtils.generateToken(email);

            // Tạo HttpOnly Cookie
            Cookie jwtCookie = new Cookie("Access_token", token);
            jwtCookie.setHttpOnly(true);     // JS không đọc được
            jwtCookie.setSecure(false);      // true nếu dùng HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60);

            response.addCookie(jwtCookie);

//            // Trả JSON thay vì redirect
//            response.setContentType("application/json");
//            response.setCharacterEncoding("UTF-8");
//
//            response.getWriter().write("""
//            {
//                "status": "success",
//                "message": "OAuth2 login successful"
//            }
//        """);

            response.sendRedirect("/book-now/home");
        }
    }
}
