package com.decorleads.service;

import com.decorleads.model.Lead;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.*;

@Component
public class GooglePlacesProvider implements LeadProvider {

    private final RestClient http = RestClient.create();
    @Value("${google.places.key}") private String apiKey;

    public String name() { return "GooglePlaces"; }

    public List<Lead> search(String keyword, String location) {
        if (apiKey.isBlank()) throw new IllegalStateException("GOOGLE_PLACES_KEY not set");
        Map<String, Object> body = Map.of(
                "textQuery", keyword + " in " + location + ", USA",
                "regionCode", "US",
                "pageSize", 20);

        JsonNode res = http.post()
                .uri("https://places.googleapis.com/v1/places:searchText")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask",
                        "places.id,places.displayName,places.websiteUri,places.formattedAddress,places.nationalPhoneNumber")
                .body(body)
                .retrieve().body(JsonNode.class);

        List<Lead> out = new ArrayList<>();
        if (res == null || !res.has("places")) return out;
        for (JsonNode p : res.get("places")) {
            Lead l = new Lead();
            l.id = p.path("id").asText(UUID.randomUUID().toString());
            l.company = p.path("displayName").path("text").asText("");
            l.website = p.path("websiteUri").asText("");
            l.address = p.path("formattedAddress").asText("");
            l.phone = p.path("nationalPhoneNumber").asText("");
            l.domain = domainOf(l.website);
            l.source = name();
            out.add(l);
        }
        return out;
    }

    static String domainOf(String url) {
        try {
            if (url == null || url.isBlank()) return "";
            String host = URI.create(url).getHost();
            return host == null ? "" : host.replaceFirst("^www\\.", "");
        } catch (Exception e) { return ""; }
    }
}
