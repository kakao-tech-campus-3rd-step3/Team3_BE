package com.shootdoori.match.service;

import com.shootdoori.match.dto.MatchCreateRequestDto;
import com.shootdoori.match.dto.MatchCreateResponseDto;
import com.shootdoori.match.dto.MatchWaitingCancelResponseDto;
import com.shootdoori.match.dto.MatchWaitingResponseDto;
import com.shootdoori.match.entity.*;
import com.shootdoori.match.exception.NoPermissionException;
import com.shootdoori.match.exception.NotFoundException;
import com.shootdoori.match.exception.ErrorCode;
import com.shootdoori.match.repository.MatchWaitingRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.repository.VenueRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
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

  @Transactional
  public MatchCreateResponseDto createMatch(Long loginUserId, MatchCreateRequestDto dto) {
    TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

    TeamMemberRole loginUserRole = teamMember.getRole();

    if(loginUserRole != TeamMemberRole.LEADER ){
      throw new NoPermissionException();
    }

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
        saved.getTeam().getTeamName(),
        saved.getMatchWaitingStatus(),
        saved.getExpiresAt()
        );
  }

  @Transactional
  public MatchWaitingCancelResponseDto cancelMatchWaiting(Long loginUserId, Long matchWaitingId){
    MatchWaiting matchWaiting = matchWaitingRepository.findById(matchWaitingId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_WAITING_NOT_FOUND, String.valueOf(matchWaitingId)));

    TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

    TeamMemberRole loginUserRole = teamMember.getRole();

    if(TeamMemberRole.isNotLeader(loginUserRole)){
      throw new NoPermissionException();
    }

    Team matchWaitingCancelteam = teamMember.getTeam();
    Team existMatchWaitingTeam = matchWaiting.getTeam();

    if(!matchWaitingCancelteam.equals(existMatchWaitingTeam)){
      throw new NoPermissionException();
    }

    matchWaiting.cancelMatchWaiting();

    return new MatchWaitingCancelResponseDto(
      matchWaiting.getWaitingId(),
      matchWaiting.getTeam().getTeamId(),
      matchWaiting.getTeam().getTeamName(),
      matchWaiting.getMatchWaitingStatus(),
      matchWaiting.getExpiresAt()
    );

  }

  @Transactional(readOnly = true)
  public Slice<MatchWaitingResponseDto> getMyWaitingMatches(Long loginUserId, Pageable pageable) {
    TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

    Long loginUserTeamId = teamMember.getTeam().getTeamId();

    Slice<MatchWaiting> myTeamMatchWaiting = matchWaitingRepository.findMyTeamMatchWaitingHistory(loginUserTeamId, pageable);

    return myTeamMatchWaiting.map(MatchWaitingResponseDto::from);
  }

}
