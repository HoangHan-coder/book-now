package vn.edu.fpt.booknow.conponents;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Base64;
@Component
public class CookieUtils {

    public static String serialize(Object object) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(object);
            objectStream.close();
            return Base64.getUrlEncoder().encodeToString(byteStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Cookie serialization failed", e);
        }
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
            ObjectInputStream objectStream =
                    new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object object = objectStream.readObject();
            objectStream.close();
            return cls.cast(object);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cookie deserialization failed", e);
        }
    }

    public static void deleteCookie(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String name) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) return;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    public static void addCookie(HttpServletResponse response,
                                 String name,
                                 String value,
                                 int maxAge) {

        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);  // true nếu HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);

        // Fix SameSite
        response.setHeader("Set-Cookie",
                String.format("%s=%s; Max-Age=%d; Path=/; HttpOnly",
                        name, value, maxAge));
    }
}
