package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VenueNotFoundException extends BusinessException {
  public VenueNotFoundException(Long venueId) {
    super(ErrorCode.VENUE_NOT_FOUND, venueId.toString());
  }
}
