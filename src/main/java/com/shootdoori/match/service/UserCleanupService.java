package com.shootdoori.match.service;

import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserCleanupService {
    private final PasswordOtpTokenRepository passwordOtpTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamMemberService teamMemberService;
    private final ProfileRepository profileRepository;

    public UserCleanupService(PasswordOtpTokenRepository passwordOtpTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository, RefreshTokenRepository refreshTokenRepository, TeamMemberRepository teamMemberRepository, TeamMemberService teamMemberService, ProfileRepository profileRepository) {
        this.passwordOtpTokenRepository = passwordOtpTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamMemberService = teamMemberService;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public void permanentlyDeleteUsers(List<User> users) {
        for (User user : users) {
            Long userId = user.getId();

            passwordOtpTokenRepository.deleteAllByUserId(userId);
            passwordResetTokenRepository.deleteAllByUserId(userId);
            refreshTokenRepository.deleteAllByUserId(userId);

            teamMemberRepository.findByUser_Id(userId).ifPresent(teamMember -> {
                Team team = teamMember.getTeam();
                teamMemberService.leave(team.getTeamId(), userId);

                // TODO: 성사된 매치(대기중), 상대팀에게 보낸 매치 신청, 매치 완료된 기록 삭제
            });

            profileRepository.deleteById(userId);
        }
    }
}
