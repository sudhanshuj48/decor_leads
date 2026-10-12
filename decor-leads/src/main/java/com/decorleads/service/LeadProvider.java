package com.decorleads.service;

import com.decorleads.model.Lead;
import java.util.List;

/** Implement this to plug in another discovery API (Yelp, Apollo, Foursquare, ...). */
public interface LeadProvider {
    String name();
    List<Lead> search(String keyword, String location);
}
