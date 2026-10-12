package com.decorleads.model;

public class Lead {
    public String id;
    public String company;
    public String website;
    public String domain;
    public String address;
    public String phone;
    public String email;      // filled by enrichment
    public String contactName;
    public String position;
    public String source;     // which API found it
    public String status = "NEW"; // NEW, ENRICHED, NO_EMAIL, SENT, FAILED, UNSUBSCRIBED
}
