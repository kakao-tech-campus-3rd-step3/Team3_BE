package com.shootdoori.match.entity.mercenary;

import com.shootdoori.match.entity.common.AuditInfo;
import com.shootdoori.match.entity.common.Position;
import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.team.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class MercenaryRecruitment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID", nullable = false)
    private Team team;

    @Column(name = "MATCH_DATE", nullable = false)
    private LocalDate matchDate;

    @Column(name = "MATCH_TIME", nullable = false)
    private LocalTime matchTime;

    @Column(name = "MESSAGE", length = 100)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "POSITION", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '골키퍼'")
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "SKILL_LEVEL", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '아마추어'")
    private SkillLevel skillLevel = SkillLevel.AMATEUR;

    @Enumerated(EnumType.STRING)
    @Column(name = "RECRUITMENT_STATUS", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '모집중'")
    private RecruitmentStatus recruitmentStatus;

    @Embedded
    private AuditInfo audit = new AuditInfo();

    protected MercenaryRecruitment() {
    }

    private MercenaryRecruitment(Team team, LocalDate matchDate, LocalTime matchTime, String message, Position position, SkillLevel skillLevel) {
        validateTeam(team);
        validate(matchDate, matchTime, message, position, skillLevel);
        this.team = team;
        this.matchDate = matchDate;
        this.matchTime = matchTime;
        this.message = message;
        this.position = position;
        this.skillLevel = skillLevel;
        this.recruitmentStatus = RecruitmentStatus.RECRUITING;
    }

    public static MercenaryRecruitment create(Team team, LocalDate matchDate, LocalTime matchTime, String message, Position position, SkillLevel skillLevel) {
        return new MercenaryRecruitment(team, matchDate, matchTime, message, position, skillLevel);
    }

    private void validate(LocalDate matchDate, LocalTime matchTime, String message, Position position, SkillLevel skillLevel) {
        validateMatchDate(matchDate);
        validateMatchTime(matchTime);
        validateMatchDateTime(matchDate, matchTime);
        validateMessage(message);
        validatePosition(position);
        validateSkillLevel(skillLevel);
    }

    private void validateTeam(Team team) {
        if (team == null) {
            throw new IllegalArgumentException("팀 정보는 필수입니다.");
        }
    }

    private void validateMatchDate(LocalDate matchDate) {
        if (matchDate == null) {
            throw new IllegalArgumentException("경기 날짜는 필수입니다.");
        }
    }

    private void validateMatchTime(LocalTime matchTime) {
        if (matchTime == null) {
            throw new IllegalArgumentException("경기 시간은 필수입니다.");
        }
    }

    private void validateMatchDateTime(LocalDate matchDate, LocalTime matchTime) {
        LocalDateTime matchDateTime = LocalDateTime.of(matchDate, matchTime);
        if (matchDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("경기 시간은 현재 시간 이후여야 합니다.");
        }
    }

    private void validateMessage(String message) {
        if (message != null && message.length() > 100) {
            throw new IllegalArgumentException("모집 메세지는 100자를 초과할 수 없습니다.");
        }
    }

    private void validatePosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("포지션 정보는 필수입니다.");
        }
    }

    private void validateSkillLevel(SkillLevel skillLevel) {
        if (skillLevel == null) {
            throw new IllegalArgumentException("요구 실력 정보는 필수입니다.");
        }
    }

    public Long getId() {
        return this.id;
    }

    public Team getTeam() {
        return this.team;
    }

    public LocalDate getMatchDate() {
        return this.matchDate;
    }

    public LocalTime getMatchTime() {
        return this.matchTime;
    }

    public String getMessage() {
        return this.message;
    }

    public Position getPosition() {
        return this.position;
    }

    public SkillLevel getSkillLevel() {
        return this.skillLevel;
    }

    public RecruitmentStatus getRecruitmentStatus() {
        return this.recruitmentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return audit.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return audit.getUpdatedAt();
    }

    public AuditInfo getAudit() {
        return audit;
    }

    public void updateRecruitmentInfo(LocalDate matchDate, LocalTime matchTime, String message, Position position, SkillLevel skillLevel) {
        validate(matchDate, matchTime, message, position, skillLevel);
        this.matchDate = matchDate;
        this.matchTime = matchTime;
        this.message = message;
        this.position = position;
        this.skillLevel = skillLevel;
    }
}
