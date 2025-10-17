package com.shootdoori.match.service;

import com.shootdoori.match.dto.LineupRequestDto;
import com.shootdoori.match.dto.LineupResponseDto;
import com.shootdoori.match.entity.lineup.Lineup;
import com.shootdoori.match.exception.common.CreationFailException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LineupService {

    private final LineupRepository lineupRepository;
    private final MatchRepository matchRepository;
    private final MatchWaitingRepository matchWaitingRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final TeamMemberRepository teamMemberRepository;

    public LineupService(LineupRepository lineupRepository,
                         MatchRepository matchRepository,
                         MatchWaitingRepository matchWaitingRepository,
                         MatchRequestRepository matchRequestRepository,
                         TeamMemberRepository teamMemberRepository) {
        this.lineupRepository = lineupRepository;
        this.matchRepository = matchRepository;
        this.matchWaitingRepository = matchWaitingRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Transactional
    public LineupResponseDto createLineup(LineupRequestDto requestDto) {
        Lineup lineup = new Lineup(matchRepository.getReferenceById(requestDto.matchId()),
                matchWaitingRepository.getReferenceById(requestDto.waitingId()),
                matchRequestRepository.getReferenceById(requestDto.requestId()),
                teamMemberRepository.getReferenceById(requestDto.teamMemberId()),
                requestDto.position(),
                requestDto.isStarter()
        );
        try {
            Lineup savedLineup = lineupRepository.saveAndFlush(lineup);
            return LineupResponseDto.from(savedLineup);
        } catch (DataIntegrityViolationException e) {
            throw new CreationFailException(ErrorCode.LINEUP_CREATION_FAILED);
        }
    }

    public List<LineupResponseDto> getAllLineupsByTeamId(Long teamId) {
        return lineupRepository.findByTeamMemberTeamTeamId(teamId).stream()
                .map(LineupResponseDto::from)
                .collect(Collectors.toList());
    }

    public LineupResponseDto getLineupById(Long id) {
        Lineup lineup = lineupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));
        return LineupResponseDto.from(lineup);
    }

    public Lineup findByIdForEntity(Long id) {
        return lineupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));
    }

    @Transactional
    public LineupResponseDto updateLineup(Long id, LineupRequestDto requestDto) {
        Lineup lineup = lineupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));

        lineup.update(matchRepository.getReferenceById(requestDto.matchId()),
                matchWaitingRepository.getReferenceById(requestDto.waitingId()),
                matchRequestRepository.getReferenceById(requestDto.requestId()),
                requestDto.position(),
                requestDto.isStarter());

        return LineupResponseDto.from(lineup);
    }

    @Transactional
    public void deleteLineup(Long id) {
        if (!lineupRepository.existsById(id)) {
            throw new NotFoundException(ErrorCode.LINEUP_NOT_FOUND);
        }
        lineupRepository.deleteById(id);
    }
}
