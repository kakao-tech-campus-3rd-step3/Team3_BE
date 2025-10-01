package com.shootdoori.match.service;

import com.shootdoori.match.dto.MatchCreateRequestDto;
import com.shootdoori.match.dto.MatchCreateResponseDto;
import com.shootdoori.match.entity.*;
import com.shootdoori.match.exception.NotFoundException;
import com.shootdoori.match.exception.ErrorCode;
import com.shootdoori.match.repository.MatchWaitingRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.repository.VenueRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
@Transactional
public class MatchCreateService {

  private final MatchWaitingRepository matchWaitingRepository;
  private final TeamRepository teamRepository;
  private final VenueRepository venueRepository;
  private final TeamMemberRepository teamMemberRepository;

  public MatchCreateService(MatchWaitingRepository matchWaitingRepository,
                            TeamRepository teamRepository,
                            VenueRepository venueRepository,
                            TeamMemberRepository teamMemberRepository) {
    this.matchWaitingRepository = matchWaitingRepository;
    this.teamRepository = teamRepository;
    this.venueRepository = venueRepository;
    this.teamMemberRepository = teamMemberRepository;
  }

  public MatchCreateResponseDto createMatch(Long loginUserId, MatchCreateRequestDto dto) {
    TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

    Team team = teamMember.getTeam();

    Venue venue = venueRepository.findById(dto.preferredVenueId())
        .orElseThrow(() -> new NotFoundException(ErrorCode.VENUE_NOT_FOUND, String.valueOf(dto.preferredVenueId())));

    MatchWaiting matchWaiting = new MatchWaiting(
        team,
        dto.preferredDate(),
        dto.preferredTimeStart(),
        dto.preferredTimeEnd(),
        venue,
        dto.skillLevelMin(),
        dto.skillLevelMax(),
        dto.universityOnly() != null ? dto.universityOnly() : false,
        dto.message(),
        MatchWaitingStatus.WAITING,
        LocalDateTime.now().plusHours(24)
    );

    MatchWaiting saved = matchWaitingRepository.save(matchWaiting);
    return new MatchCreateResponseDto(
        saved.getWaitingId(),
        saved.getTeam().getTeamId(),
        saved.getMatchRequestStatus(),
        saved.getExpiresAt()
        );
  }
}
