package com.shootdoori.match.service;

import com.shootdoori.match.dto.VenueSearchResponseDto;
import com.shootdoori.match.entity.Venue;
import com.shootdoori.match.repository.VenueRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional(readOnly = true)
    public Slice<VenueSearchResponseDto> getAllVenues(Pageable pageable) {
        Slice<Venue> venues = venueRepository.findAll(pageable);

        return venues.map(v -> new VenueSearchResponseDto(
            v.getVenueId(),
            v.getVenueName(),
            v.getAddress(),
            v.getContactInfo(),
            v.getFacilities(),
            v.getPricePerHour()
        ));
    }
}
