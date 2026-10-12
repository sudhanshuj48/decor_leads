package com.decorleads.web;

import com.decorleads.model.Lead;
import com.decorleads.service.LeadService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ApiController {

    private final LeadService service;
    public ApiController(LeadService service) { this.service = service; }

    public record SearchReq(String keyword, String location) {}
    public record SendReq(List<String> leadIds, String subject, String body) {}

    @GetMapping("/api/leads")
    public List<Lead> leads() { return service.all(); }

    @PostMapping("/api/leads/search")
    public List<Lead> search(@RequestBody SearchReq r) {
        String kw = (r.keyword() == null || r.keyword().isBlank()) ? "home decor store" : r.keyword();
        return service.search(kw, r.location());
    }

    @PostMapping("/api/leads/enrich")
    public Map<String, Integer> enrich() { return Map.of("emailsFound", service.enrichAll()); }

    @PostMapping("/api/email/send")
    public Map<String, Integer> send(@RequestBody SendReq r) {
        return service.send(r.leadIds(), r.subject(), r.body());
    }

    @RequestMapping(value = "/unsubscribe", method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.TEXT_HTML_VALUE)
    public String unsubscribe(@RequestParam String id) {
        service.unsubscribe(id);
        return "<h3>You have been unsubscribed and will not receive further emails.</h3>";
    }
}
