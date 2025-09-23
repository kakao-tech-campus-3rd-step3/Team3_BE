package com.shootdoori.match.service;

import com.shootdoori.match.dto.RecruitmentCreateRequest;
import com.shootdoori.match.dto.RecruitmentResponse;
import com.shootdoori.match.dto.RecruitmentUpdateRequest;
import com.shootdoori.match.entity.MercenaryRecruitment;
import com.shootdoori.match.entity.Position;
import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.exception.RecruitmentNotFoundException;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.repository.MercenaryRecruitmentRepository;
import com.shootdoori.match.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MercenaryRecruitmentService {
    private final MercenaryRecruitmentRepository recruitmentRepository;
    private final TeamRepository teamRepository;

    public MercenaryRecruitmentService(MercenaryRecruitmentRepository recruitmentRepository, TeamRepository teamRepository) {
        this.recruitmentRepository = recruitmentRepository;
        this.teamRepository = teamRepository;
    }

    public RecruitmentResponse create(RecruitmentCreateRequest request) {
        // TODO: 요청을 보낸 사용자가 이 게시글을 생성할 권한이 있는지 확인하는 로직 추가

        Team team = teamRepository.findById(request.teamId()).orElseThrow(
            () -> new TeamNotFoundException(request.teamId()));

        Position position = Position.fromDisplayName(request.position());
        SkillLevel skillLevel = SkillLevel.fromDisplayName(request.skillLevel());

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
            .orElseThrow(RecruitmentNotFoundException::new);

        return new RecruitmentResponse(recruitment);
    }

    public RecruitmentResponse update(Long id, RecruitmentUpdateRequest updateRequest) {
        // TODO: 요청을 보낸 사용자가 이 게시글을 수정할 권한이 있는지 확인하는 로직 추가

        MercenaryRecruitment recruitment = recruitmentRepository.findById(id)
            .orElseThrow(RecruitmentNotFoundException::new);

        Position position = Position.fromDisplayName(updateRequest.position());
        SkillLevel skillLevel = SkillLevel.fromDisplayName(updateRequest.skillLevel());

        recruitment.updateRecruitmentInfo(
            updateRequest.matchDate(), updateRequest.matchTime(), updateRequest.message(), position, skillLevel);

        return new RecruitmentResponse(recruitment);
    }

    public void delete(Long id) {
        // TODO: 요청을 보낸 사용자가 이 게시글을 삭제할 권한이 있는지 확인하는 로직 추가

        recruitmentRepository.findById(id)
            .orElseThrow(RecruitmentNotFoundException::new);

        recruitmentRepository.deleteById(id);
    }
}
