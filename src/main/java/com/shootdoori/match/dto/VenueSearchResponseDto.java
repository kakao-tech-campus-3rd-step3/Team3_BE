package com.shootdoori.match.dto;

public record VenueSearchResponseDto(
  String venueName,
  String address,
  String contactInfo,
  String facilities,
  Long pricePerHour
) {}
