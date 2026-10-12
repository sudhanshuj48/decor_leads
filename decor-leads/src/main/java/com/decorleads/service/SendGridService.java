package com.decorleads.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class SendGridService {

    private final RestClient http = RestClient.create();
    @Value("${sendgrid.key}") private String apiKey;
    @Value("${mail.from.email}") private String fromEmail;
    @Value("${mail.from.name}") private String fromName;

    public void send(String to, String subject, String html, String unsubscribeUrl) {
        if (apiKey.isBlank()) throw new IllegalStateException("SENDGRID_KEY not set");
        Map<String, Object> payload = Map.of(
                "personalizations", List.of(Map.of("to", List.of(Map.of("email", to)))),
                "from", Map.of("email", fromEmail, "name", fromName),
                "subject", subject,
                "content", List.of(Map.of("type", "text/html", "value", html)),
                "headers", Map.of(
                        "List-Unsubscribe", "<" + unsubscribeUrl + ">",
                        "List-Unsubscribe-Post", "List-Unsubscribe=One-Click"));

        http.post().uri("https://api.sendgrid.com/v3/mail/send")
                .header("Authorization", "Bearer " + apiKey)
                .body(payload)
                .retrieve().toBodilessEntity();
    }
}
