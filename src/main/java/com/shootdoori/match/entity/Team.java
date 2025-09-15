package com.shootdoori.match.entity;

import com.shootdoori.match.exception.DifferentUniversityException;
import com.shootdoori.match.exception.TeamCapacityExceededException;
import com.shootdoori.match.value.Description;
import com.shootdoori.match.value.MemberCount;
import com.shootdoori.match.value.TeamName;
import com.shootdoori.match.value.UniversityName;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TEAM_ID")
    private Long teamId;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "TEAM_NAME", nullable = false, length = 100))
    private TeamName teamName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CAPTAIN_ID", nullable = false)
    private User captain;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "UNIVERSITY", nullable = false, length = 100))
    private UniversityName university;

    @Enumerated(EnumType.STRING)
    @Column(name = "TEAM_TYPE", nullable = false, length = 20)
    private TeamType teamType = TeamType.OTHER;

    @Embedded
    @AttributeOverride(name = "count", column = @Column(name = "MEMBER_COUNT", nullable = false))
    private MemberCount memberCount = MemberCount.of(1);

    @Enumerated(EnumType.STRING)
    @Column(name = "SKILL_LEVEL", nullable = false, length = 20)
    private SkillLevel skillLevel = SkillLevel.AMATEUR;

    @Embedded
    @AttributeOverride(name = "description", column = @Column(name = "DESCRIPTION", length = 1000))
    private Description description;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "team", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TeamMember> memberList = new ArrayList<>();

    private static final int MAX_MEMBER_COUNT = 100;

    protected Team() {
    }

    public Team(String teamName, User captain, String university, TeamType teamType,
        SkillLevel skillLevel, String description) {
        this.teamName = TeamName.of(teamName);
        this.captain = captain;
        this.university = UniversityName.of(university);
        this.teamType = teamType != null ? teamType : TeamType.OTHER;
        this.skillLevel = skillLevel != null ? skillLevel : SkillLevel.AMATEUR;
        this.description = Description.of(description);
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public Long getTeamId() {
        return teamId;
    }

    public TeamName getTeamName() {
        return teamName;
    }

    public User getCaptain() {
        return captain;
    }

    public UniversityName getUniversity() {
        return university;
    }

    public TeamType getTeamType() {
        return teamType;
    }

    public MemberCount getMemberCount() {
        return memberCount;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public Description getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<TeamMember> getMemberList() {
        return memberList;
    }

    public void addMember(TeamMember member) {
        memberList.add(member);
        member.setTeam(this);
        this.memberCount = this.memberCount.increase();
    }

    public void removeMember(TeamMember member) {
        memberList.remove(member);
        member.setTeam(null);
        this.memberCount = this.memberCount.decrease();
    }

    public void changeTeamInfo(String teamName,
        String university,
        String skillLevel,
        String description) {

        this.teamName = TeamName.of(teamName);
        this.university = UniversityName.of(university);
        this.skillLevel = SkillLevel.fromDisplayName(skillLevel);
        this.description = Description.of(description);
    }

    public void validateSameUniversity(User user) {
        if (!this.university.equals(user.getUniversity())) {
            throw new DifferentUniversityException("팀 소속 대학과 동일한 대학의 사용자만 가입할 수 있습니다.");
        }
    }

    public void validateCanAcceptNewMember() {
        if (this.memberCount.count() >= MAX_MEMBER_COUNT) {
            throw new TeamCapacityExceededException("팀 정원이 가득 찼습니다. (최대 100명)");
        }
    }
}
