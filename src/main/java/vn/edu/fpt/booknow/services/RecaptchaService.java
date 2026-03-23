package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String secretKey;

    public boolean verify(String recaptchaResponse) {

        String url = "https://www.google.com/recaptcha/api/siteverify";

        RestTemplate restTemplate = new RestTemplate();

        // Body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", recaptchaResponse);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Combine body + headers
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        // Call API
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                request,
                Map.class
        );

        Map responseBody = response.getBody();

        return Boolean.TRUE.equals(responseBody.get("success"));
    }
}