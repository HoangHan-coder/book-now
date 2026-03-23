package vn.edu.fpt.booknow.conponents;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final String SECRET =
            "$2a$12$P9xajysX2yD1RX2jHl52FepC1fxGGlCY7fABfWKAJc3e6BUggywVq";

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(String email) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + 86400000);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}