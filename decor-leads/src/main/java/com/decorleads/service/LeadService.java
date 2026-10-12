package com.decorleads.service;

import com.decorleads.model.Lead;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LeadService {

    private final Map<String, Lead> leads = new ConcurrentHashMap<>();
    private final Set<String> suppressed = ConcurrentHashMap.newKeySet(); // unsubscribed emails

    private final List<LeadProvider> providers;
    private final HunterService hunter;
    private final SendGridService mail;

    @Value("${app.public.url}") private String publicUrl;
    @Value("${mail.postal.address}") private String postalAddress;

    public LeadService(List<LeadProvider> providers, HunterService hunter, SendGridService mail) {
        this.providers = providers; this.hunter = hunter; this.mail = mail;
    }

    public List<Lead> all() { return new ArrayList<>(leads.values()); }

    /** Query every registered provider and merge by domain/company. */
    public List<Lead> search(String keyword, String location) {
        List<Lead> found = new ArrayList<>();
        for (LeadProvider p : providers) {
            try {
                for (Lead l : p.search(keyword, location)) {
                    boolean dup = leads.values().stream().anyMatch(x ->
                            (!l.domain.isBlank() && l.domain.equals(x.domain)) || l.id.equals(x.id));
                    if (!dup) { leads.put(l.id, l); found.add(l); }
                }
            } catch (Exception e) {
                System.err.println(p.name() + " failed: " + e.getMessage());
            }
        }
        return found;
    }

    public int enrichAll() {
        int n = 0;
        for (Lead l : leads.values()) {
            if (!"NEW".equals(l.status)) continue;
            try { hunter.enrich(l); if (l.email != null) n++; }
            catch (Exception e) { System.err.println("Hunter failed for " + l.domain + ": " + e.getMessage()); }
        }
        return n;
    }

    public Map<String, Integer> send(List<String> ids, String subject, String body) {
        int sent = 0, skipped = 0, failed = 0;
        for (String id : ids) {
            Lead l = leads.get(id);
            if (l == null || l.email == null || suppressed.contains(l.email.toLowerCase())
                    || "SENT".equals(l.status)) { skipped++; continue; }
            try {
                String personalised = body.replace("{{company}}", l.company)
                        .replace("{{name}}", l.contactName == null || l.contactName.isBlank() ? "there" : l.contactName);
                String unsub = publicUrl + "/unsubscribe?id=" + l.id;
                String html = HtmlUtils.htmlEscape(personalised).replace("\n", "<br>")
                        + "<hr><p style='font-size:12px;color:#666'>" + HtmlUtils.htmlEscape(postalAddress)
                        + "<br>Don't want these emails? <a href='" + unsub + "'>Unsubscribe</a></p>";
                mail.send(l.email, subject, html, unsub);
                l.status = "SENT"; sent++;
                Thread.sleep(300); // gentle throttling
            } catch (Exception e) {
                l.status = "FAILED"; failed++;
                System.err.println("Send failed for " + l.email + ": " + e.getMessage());
            }
        }
        return Map.of("sent", sent, "skipped", skipped, "failed", failed);
    }

    public void unsubscribe(String id) {
        Lead l = leads.get(id);
        if (l != null && l.email != null) { suppressed.add(l.email.toLowerCase()); l.status = "UNSUBSCRIBED"; }
    }
}
