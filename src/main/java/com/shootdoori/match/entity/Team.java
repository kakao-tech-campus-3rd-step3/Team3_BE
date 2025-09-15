package com.shootdoori.match.entity;

import com.shootdoori.match.exception.DuplicateMemberException;
import com.shootdoori.match.exception.TeamFullException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TEAM_ID")
    private Long teamId;

    @Column(name = "TEAM_NAME", nullable = false, length = 100)
    private String teamName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CAPTAIN_ID", nullable = false)
    private User captain;

    @Column(name = "UNIVERSITY", nullable = false, length = 100)
    private String university;

    @Enumerated(EnumType.STRING)
    @Column(name = "TEAM_TYPE", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '동아리'")
    private TeamType teamType = TeamType.OTHER;

    @Column(name = "MEMBER_COUNT", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer memberCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "SKILL_LEVEL", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '아마추어'")
    private SkillLevel skillLevel = SkillLevel.AMATEUR;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "teams", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TeamMember> members = new ArrayList<>();


    private static final int MIN_MEMBERS = 1;
    private static final int MAX_MEMBERS = 100;

    protected Team() {
    }

    public Team(String teamName, User captain, String university, TeamType teamType,
        SkillLevel skillLevel, String description) {
        this.teamName = teamName;
        this.captain = captain;
        this.university = university;
        this.teamType = teamType != null ? teamType : TeamType.OTHER;
        this.skillLevel = skillLevel != null ? skillLevel : SkillLevel.AMATEUR;
        this.description = description;
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

    public String getTeamName() {
        return teamName;
    }

    public User getCaptain() {
        return captain;
    }

    public String getUniversity() {
        return university;
    }

    public TeamType getTeamType() {
        return teamType;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public String getDescription() {
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
    }

    public void removeMember(TeamMember member) {
        members.remove(member);
        member.setTeam(null);
    }

    public void increaseMemberCount() {
        validateMemberCount(this.memberCount + 1);
        this.memberCount++;
    }

    public void decreaseMemberCount() {
        validateMemberCount(this.memberCount - 1);
        this.memberCount--;
    }

    public void changeTeamInfo(String teamName,
        String university,
        String skillLevel,
        String description) {

        validateTeamName(teamName);
        validateUniversity(university);
        validateDescription(description);

        this.teamName = teamName;
        this.university = university;
        this.skillLevel = SkillLevel.fromDisplayName(skillLevel);
        this.description = description;
    }
}
