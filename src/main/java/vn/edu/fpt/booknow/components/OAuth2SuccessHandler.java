package vn.edu.fpt.booknow.components;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.services.customer.CustomerService;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    CustomerService userService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) auth.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String fullName = oauthUser.getAttribute("");
        Customer customer = userService.findOrCreateGoogleUser(email);

        String jwt = jwtService.generateToken(user);

        res.addHeader("Authorization", "Bearer " + jwt);
        res.sendRedirect("/home");
    }
}

