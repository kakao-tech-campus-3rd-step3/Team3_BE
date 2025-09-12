package com.shootdoori.match.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "venues")
public class Venue {

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

  @Column(name = "CREATED_AT", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "UPDATED_AT", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;

  protected Venue() {}

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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
