package vn.edu.fpt.booknow.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.model.entities.StaffAccount;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class JWTService {
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60;
    private final String SECRET_KEY = "$2a$12$P9xajysX2yD1RX2jHl52FepC1fxGGlCY7fABfWKAJc3e6BUggywVq";

    public String generateToken(String username) {
        Map<String, Objects> claims = new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .and()
                .signWith(getKey())
                .compact();
    }

    private void createCookie(StaffAccount staffAccount, Customer customer, HttpServletResponse response) {
        String token = null;
        if (staffAccount != null) {
            token = generateToken(staffAccount.getEmail());
        } else if (customer != null) {
            token = generateToken(customer.getEmail());
        }
        Cookie cookie = new Cookie("Access_token", token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public void createCookie(Customer customer, HttpServletResponse response) {
        createCookie(null, customer, response);
    }

    public void createCookie(StaffAccount staffAccount, HttpServletResponse response) {
        createCookie(staffAccount, null, response);
    }

    public void removeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("Access_token", "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }


    private SecretKey getKey() {
        byte[] keyByte = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyByte);
    }

    public String extractUserName(String token) throws Exception{
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) throws Exception{
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws Exception{
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    private boolean isTokenExpired(String token) throws Exception {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) throws Exception {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean validateToken(String token, UserDetails userDetails) throws Exception{
        String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
