# Home Decor Buyer Finder (Java 17 + Spring Boot + REST)

Flow: Google Places API (find US decor retailers) -> Hunter.io API (find business email by domain) -> SendGrid API (send email).

## Run
    export GOOGLE_PLACES_KEY=...   # Places API (New) enabled
    export HUNTER_KEY=...
    export SENDGRID_KEY=...        # verify your sender/domain in SendGrid
    export MAIL_FROM_EMAIL=sales@yourdomain.com
    export MAIL_FROM_NAME="Your Company"
    export MAIL_POSTAL_ADDRESS="Your Company, Street, City, Country"
    export APP_PUBLIC_URL=https://your-public-host   # unsubscribe links must be reachable
    mvn spring-boot:run
Open http://localhost:8080

## REST endpoints
- GET  /api/leads
- POST /api/leads/search   {"keyword":"home decor store","location":"Austin, TX"}
- POST /api/leads/enrich
- POST /api/email/send     {"leadIds":["..."],"subject":"...","body":"Hi {{name}}..."}
- GET  /unsubscribe?id=...

## Add another source
Implement `LeadProvider` (Yelp, Apollo, Foursquare...) and annotate with @Component; it is auto-included in every search.

## Compliance (US CAN-SPAM)
Real sender identity, postal address, honest subject, working unsubscribe (included) - honour opt-outs. Data is in-memory; add a DB to persist the suppression list.
