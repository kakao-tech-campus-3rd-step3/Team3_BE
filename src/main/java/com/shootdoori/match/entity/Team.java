package com.shootdoori.match.entity;

import com.shootdoori.match.value.Description;
import com.shootdoori.match.value.MemberCount;
import com.shootdoori.match.value.TeamName;
import com.shootdoori.match.value.UniversityName;
import jakarta.persistence.AttributeOverride;
import com.shootdoori.match.exception.DuplicateMemberException;
import com.shootdoori.match.exception.TeamFullException;
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
    private List<TeamMember> members = new ArrayList<>();


    private static final int MIN_MEMBERS = 1;
    private static final int MAX_MEMBERS = 100;

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

    public List<TeamMember> getMembers() {
        return members;
    }

    private void validateTeamName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("팀 이름은 필수입니다.");
        }
        if (name.length() > MAX_MEMBERS) {
            throw new IllegalArgumentException("팀 이름은 최대 100자입니다.");
        }
    }

    private void validateUniversity(String university) {
        if (university == null || university.isBlank()) {
            throw new IllegalArgumentException("대학교는 필수입니다.");
        }
        if (university.length() > MAX_MEMBERS) {
            throw new IllegalArgumentException("대학교는 최대 100자입니다.");
        }
    }

    private void validateDescription(String desc) {
        if (desc != null && desc.length() > 1000) {
            throw new IllegalArgumentException("설명은 최대 1000자입니다.");
        }
    }

    private void validateMemberCount(int count) {
        if (count < MIN_MEMBERS || count > MAX_MEMBERS) {
            throw new IllegalArgumentException("멤버 수는 1~100명입니다.");
        }
    }

    public void recruitMember(User user, TeamMemberRole role) {
        if (members.size() >= MAX_MEMBERS) {
            throw new TeamFullException("팀 정원이 초과되었습니다.");
        }

        if (members.stream().anyMatch(member -> member.getUser().equals(user))) {
            throw new DuplicateMemberException("이미 팀에 가입된 사용자입니다.");
        }

        TeamMember teamMember = new TeamMember(this, user, role);
        this.members.add(teamMember);
        this.memberCount = this.memberCount.increase();
    }

    public void removeMember(TeamMember member) {
        members.remove(member);
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
}
