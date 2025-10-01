package com.shootdoori.match.dto;

public record VenueSearchResponseDto(
  Long venueId,
  String venueName,
  String address,
  String contactInfo,
  String facilities,
  Long pricePerHour
) {}
