package com.shootdoori.match.entity.venue;

import com.shootdoori.match.entity.common.DateEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venues")
public class Venue extends DateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VENUE_ID")
    private Long venueId;

    @Column(name = "VENUE_NAME", nullable = false, length = 100)
    private String venueName;

    @Column(name = "ADDRESS", nullable = false, length = 255)
    private String address;

    @Column(name = "LATITUDE", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "LONGITUDE", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "CONTACT_INFO", length = 100)
    private String contactInfo;

    @Column(name = "FACILITIES", columnDefinition = "TEXT")
    private String facilities;

    @Column(name = "PRICE_PER_HOUR")
    private Long pricePerHour;

    protected Venue() {
    }

    public Venue(String venueName,
                 String address,
                 BigDecimal latitude,
                 BigDecimal longitude,
                 String contactInfo,
                 String facilities,
                 Long pricePerHour) {
        this.venueName = venueName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactInfo = contactInfo;
        this.facilities = facilities;
        this.pricePerHour = pricePerHour;
    }

    public Long getVenueId() {
        return venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public String getFacilities() {
        return facilities;
    }

    public Long getPricePerHour() {
        return pricePerHour;
    }
}
