package com.shootdoori.match.service;

import com.shootdoori.match.dto.MatchCreateRequestDto;
import com.shootdoori.match.dto.MatchCreateResponseDto;
import com.shootdoori.match.entity.MatchQueue;
import com.shootdoori.match.entity.MatchQueueStatus;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.Venue;
import com.shootdoori.match.repository.MatchQueueRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.repository.VenueRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class MatchCreateService {

  private final MatchQueueRepository matchQueueRepository;
  private final TeamRepository teamRepository;
  private final VenueRepository venueRepository;

  public MatchCreateService(MatchQueueRepository matchQueueRepository,
      TeamRepository teamRepository,
      VenueRepository venueRepository) {
    this.matchQueueRepository = matchQueueRepository;
    this.teamRepository = teamRepository;
    this.venueRepository = venueRepository;
  }

  public MatchCreateResponseDto createMatch(MatchCreateRequestDto dto) {
    Team team = teamRepository.findById(dto.teamId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다. id=" + dto.teamId()));

    Venue venue = venueRepository.findById(dto.preferredVenueId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기장입니다. id=" + dto.preferredVenueId()));

    MatchQueue matchQueue = new MatchQueue(
        team,
        dto.preferredDate(),
        dto.preferredTimeStart(),
        dto.preferredTimeEnd(),
        venue,
        dto.skillLevelMin(),
        dto.skillLevelMax(),
        dto.universityOnly() != null ? dto.universityOnly() : false,
        dto.message(),
        MatchQueueStatus.WAITING,
        LocalDateTime.now().plusHours(24)
    );

    MatchQueue saved = matchQueueRepository.save(matchQueue);
    return new MatchCreateResponseDto(
        saved.getWaitingId(),
        saved.getTeam().getTeamId(),
        saved.getMatchRequestStatus(),
        saved.getExpiresAt()
        );
  }
}
