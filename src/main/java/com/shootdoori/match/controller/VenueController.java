package com.shootdoori.match.controller;

import com.shootdoori.match.dto.VenueSearchResponseDto;
import com.shootdoori.match.service.VenueService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public Slice<VenueSearchResponseDto> getAllVenues(
        @PageableDefault(size = 10, sort = "venueName", direction = Sort.Direction.ASC) Pageable pageable) {
        return venueService.getAllVenues(pageable);
    }
}
