package com.decorleads.service;

import com.decorleads.model.Lead;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Finds a public business email for a company domain via Hunter.io. */
@Service
public class HunterService {

    private final RestClient http = RestClient.create();
    @Value("${hunter.key}") private String apiKey;

    public void enrich(Lead lead) {
        if (apiKey.isBlank()) throw new IllegalStateException("HUNTER_KEY not set");
        if (lead.domain == null || lead.domain.isBlank()) { lead.status = "NO_EMAIL"; return; }

        JsonNode res = http.get()
                .uri("https://api.hunter.io/v2/domain-search?domain={d}&limit=10&api_key={k}",
                        lead.domain, apiKey)
                .retrieve().body(JsonNode.class);

        JsonNode emails = res == null ? null : res.path("data").path("emails");
        if (emails == null || !emails.isArray() || emails.isEmpty()) { lead.status = "NO_EMAIL"; return; }

        JsonNode best = null; int bestScore = -1;
        for (JsonNode e : emails) {
            int s = e.path("confidence").asInt(0);
            String pos = e.path("position").asText("").toLowerCase();
            if (pos.contains("buy") || pos.contains("purchas") || pos.contains("merchandis")
                    || pos.contains("procure") || pos.contains("owner")) s += 100; // prefer decision makers
            if (s > bestScore) { bestScore = s; best = e; }
        }
        lead.email = best.path("value").asText();
        lead.contactName = (best.path("first_name").asText("") + " " + best.path("last_name").asText("")).trim();
        lead.position = best.path("position").asText("");
        lead.status = "ENRICHED";
    }
}
