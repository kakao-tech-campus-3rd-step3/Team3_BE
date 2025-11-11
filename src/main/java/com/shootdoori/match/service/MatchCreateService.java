package com.shootdoori.match.service;

import com.shootdoori.match.dto.MatchCreateRequestDto;
import com.shootdoori.match.dto.MatchCreateResponseDto;
import com.shootdoori.match.dto.MatchWaitingCancelResponseDto;
import com.shootdoori.match.dto.MatchWaitingResponseDto;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.match.waiting.MatchWaitingStatus;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.venue.Venue;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NoPermissionException;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.MatchWaitingRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class MatchCreateService {

    private final MatchWaitingRepository matchWaitingRepository;
    private final VenueService venueService;
    private final TeamMemberService teamMemberService;

    public MatchCreateService(MatchWaitingRepository matchWaitingRepository,
                              VenueService venueService,
                              TeamMemberService teamMemberService) {
        this.matchWaitingRepository = matchWaitingRepository;
        this.venueService = venueService;
        this.teamMemberService = teamMemberService;
    }

    @Transactional
    public MatchCreateResponseDto createMatch(Long loginUserId, MatchCreateRequestDto dto) {
        TeamMember teamMember = teamMemberService.findByUserIdForEntity(loginUserId);

        if (!teamMember.isCaptain()) {
            throw new NoPermissionException(ErrorCode.CAPTAIN_ONLY_OPERATION);
        }

        Venue venue = venueService.findByIdForEntity(dto.preferredVenueId());

        MatchWaiting matchWaiting = new MatchWaiting(
            teamMember.getTeam(),
            dto.preferredDate(),
            dto.preferredTimeStart(),
            dto.preferredTimeEnd(),
            venue,
            dto.skillLevelMin(),
            dto.skillLevelMax(),
            dto.universityOnly() != null ? dto.universityOnly() : false,
            dto.message(),
            MatchWaitingStatus.WAITING,
            LocalDateTime.of(dto.preferredDate(), dto.preferredTimeEnd()),
            dto.lineupId()
        );

        MatchWaiting saved = matchWaitingRepository.save(matchWaiting);

        return MatchCreateResponseDto.from(saved);
    }

    @Transactional
    public MatchWaitingCancelResponseDto cancelMatchWaiting(Long loginUserId, Long matchWaitingId) {
        MatchWaiting matchWaiting = findByIdForEntity(matchWaitingId);

        TeamMember teamMember = teamMemberService.findByUserIdForEntity(loginUserId);

        if (!teamMember.isCaptain()) {
            throw new NoPermissionException(ErrorCode.CAPTAIN_ONLY_OPERATION);
        }

        if (!matchWaiting.belongTo(teamMember)) {
            throw new NoPermissionException(ErrorCode.MATCH_OPERATION_PERMISSION_DENIED);
        }

        matchWaiting.cancelMatchWaiting();

        return MatchWaitingCancelResponseDto.from(matchWaiting);
    }

    @Transactional(readOnly = true)
    public Slice<MatchWaitingResponseDto> getMyWaitingMatches(Long loginUserId, Pageable pageable) {
        TeamMember teamMember = teamMemberService.findByUserIdForEntity(loginUserId);

        Slice<MatchWaiting> myTeamMatchWaiting = matchWaitingRepository.findMyTeamMatchWaitingHistory(
            teamMember.getTeamId(), pageable);

        return myTeamMatchWaiting.map(MatchWaitingResponseDto::from);
    }

    @Transactional(readOnly = true)
    public MatchWaiting findByIdForEntity(Long matchWaitingId) {
        return matchWaitingRepository.findById(matchWaitingId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_WAITING_NOT_FOUND,
                String.valueOf(matchWaitingId)));
    }

    @Transactional
    public void cancelAllMatchesByTeamId(Long teamId) {
        matchWaitingRepository.cancelAllByTeamId(teamId);
    }
}
