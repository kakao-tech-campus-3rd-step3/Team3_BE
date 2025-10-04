package com.shootdoori.match.service;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileMapper;
import com.shootdoori.match.dto.ProfileResponse;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.DuplicatedException;
import com.shootdoori.match.exception.ErrorCode;
import com.shootdoori.match.exception.LeaderCannotLeaveTeamException;
import com.shootdoori.match.exception.NotFoundException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.RefreshTokenRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TeamMemberRepository teamMemberRepository;

    public ProfileService(ProfileRepository profileRepository, ProfileMapper profileMapper, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository, TeamMemberRepository teamMemberRepository) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public ProfileResponse createProfile(ProfileCreateRequest createRequest) {
        if (profileRepository.existsByEmailOrUniversityEmail(
            createRequest.email(), createRequest.universityEmail())
        ) {
            throw new DuplicatedException(ErrorCode.DUPLICATED_USER);
        }

        String encodePassword = passwordEncoder.encode(createRequest.password());

        User user = User.create(
            createRequest.name(),
            createRequest.skillLevel(),
            createRequest.email(),
            createRequest.universityEmail(),
            encodePassword,
            createRequest.kakaoTalkId(),
            createRequest.position(),
            createRequest.university(),
            createRequest.department(),
            createRequest.studentYear(),
            createRequest.bio()
        );

        User saveProfile = profileRepository.save(user);
        return profileMapper.toProfileResponse(saveProfile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse findProfileById(Long id) {
        User profile = profileRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_NOT_FOUND));

        Long teamId = teamMemberRepository.findByUser_Id(id)
            .map(teamMember -> teamMember.getTeam().getTeamId())
            .orElse(null);

        return profileMapper.toProfileResponse(profile, teamId);
    }

    @Transactional
    public Optional<User> findByEmail(String email) {
        return profileRepository.findByEmail(email);
    }

    public ProfileResponse updateProfile(Long id, ProfileUpdateRequest updateRequest) {
        User profile = profileRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_NOT_FOUND));

        profile.update(updateRequest.skillLevel(), updateRequest.position(), updateRequest.bio());

        Long teamId = teamMemberRepository.findByUser_Id(id)
            .map(teamMember -> teamMember.getTeam().getTeamId())
            .orElse(null);

        return profileMapper.toProfileResponse(profile, teamId);
    }

    public void deleteAccount(Long id) {
        User user = profileRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_NOT_FOUND));

        teamMemberRepository.findByUser_Id(id).ifPresent(teamMember -> {
            Team team = teamMember.getTeam();
            if (team.getCaptain().equals(user)) {
                throw new LeaderCannotLeaveTeamException(ErrorCode.LEADER_CANNOT_LEAVE_TEAM);
            }
        });
        user.requestDeletion();

        refreshTokenRepository.deleteAllByUserId(id);
    }
}
