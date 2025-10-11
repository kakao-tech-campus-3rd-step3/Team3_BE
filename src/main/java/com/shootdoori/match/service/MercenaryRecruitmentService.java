package com.shootdoori.match.service;

import com.shootdoori.match.dto.RecruitmentCreateRequest;
import com.shootdoori.match.dto.RecruitmentResponse;
import com.shootdoori.match.dto.RecruitmentUpdateRequest;
import com.shootdoori.match.entity.mercenary.MercenaryRecruitment;
import com.shootdoori.match.entity.mercenary.MercenaryPosition;
import com.shootdoori.match.entity.mercenary.MercenaryRecruitmentSkillLevel;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.exception.common.NoPermissionException;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.repository.MercenaryRecruitmentRepository;
import com.shootdoori.match.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class MercenaryRecruitmentService {
    private final MercenaryRecruitmentRepository recruitmentRepository;
    private final TeamRepository teamRepository;

    public MercenaryRecruitmentService(MercenaryRecruitmentRepository recruitmentRepository, TeamRepository teamRepository) {
        this.recruitmentRepository = recruitmentRepository;
        this.teamRepository = teamRepository;
    }

    public RecruitmentResponse create(RecruitmentCreateRequest request, Long loginUserId) {

        Team team = teamRepository.findById(request.teamId()).orElseThrow(
            () -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(request.teamId())));

        if(!team.getCaptain().getId().equals(loginUserId)) {
            throw new NoPermissionException();
        }

        MercenaryPosition position = MercenaryPosition.fromDisplayName(request.position());
        MercenaryRecruitmentSkillLevel skillLevel = MercenaryRecruitmentSkillLevel.fromDisplayName(request.skillLevel());

        MercenaryRecruitment savedRecruitment = recruitmentRepository.save(MercenaryRecruitment.create(
            team, request.matchDate(), request.matchTime(), request.message(), position, skillLevel));

        return new RecruitmentResponse(savedRecruitment);
    }

    @Transactional(readOnly = true)
    public Page<RecruitmentResponse> findAllPages(Pageable pageable) {
        Page<MercenaryRecruitment> recruitments = recruitmentRepository.findAll(pageable);

        return recruitments.map(RecruitmentResponse::new);
    }

    @Transactional(readOnly = true)
    public RecruitmentResponse findById(Long id) {
        MercenaryRecruitment recruitment = recruitmentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.RECRUITMENT_NOT_FOUND));

        return new RecruitmentResponse(recruitment);
    }

    public RecruitmentResponse update(Long id, RecruitmentUpdateRequest updateRequest, Long loginUserId) {

        MercenaryRecruitment recruitment = recruitmentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.RECRUITMENT_NOT_FOUND));

        if(!recruitment.getTeam().getCaptain().getId().equals(loginUserId)) {
            throw new NoPermissionException();
        }

        MercenaryPosition position = MercenaryPosition.fromDisplayName(updateRequest.position());
        MercenaryRecruitmentSkillLevel skillLevel = MercenaryRecruitmentSkillLevel.fromDisplayName(updateRequest.skillLevel());

        recruitment.updateRecruitmentInfo(
            updateRequest.matchDate(), updateRequest.matchTime(), updateRequest.message(), position, skillLevel);

        return new RecruitmentResponse(recruitment);
    }

    public void delete(Long id, Long loginUserId) {

        MercenaryRecruitment recruitment = recruitmentRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.RECRUITMENT_NOT_FOUND));

        if(!recruitment.getTeam().getCaptain().getId().equals(loginUserId)) {
            throw new NoPermissionException();
        }

        recruitmentRepository.deleteById(id);
    }
}
