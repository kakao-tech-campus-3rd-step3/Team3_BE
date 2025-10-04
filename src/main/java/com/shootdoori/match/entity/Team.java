package com.shootdoori.match.entity;

import com.shootdoori.match.exception.common.DifferentException;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.domain.team.LastTeamMemberRemovalNotAllowedException;
import com.shootdoori.match.exception.domain.team.TeamCapacityExceededException;
import com.shootdoori.match.exception.domain.team.TeamFullException;
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
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "team")
public class Team extends DateEntity {

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
    private MemberCount memberCount = MemberCount.of(0);

    @Enumerated(EnumType.STRING)
    @Column(name = "SKILL_LEVEL", nullable = false, length = 20)
    private TeamSkillLevel skillLevel = TeamSkillLevel.AMATEUR;

    @Embedded
    @AttributeOverride(name = "description", column = @Column(name = "DESCRIPTION", length = 1000))
    private Description description;

    @OneToMany(mappedBy = "team", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<TeamMember> members = new ArrayList<>();


    private static final int MIN_MEMBERS = 0;
    private static final int MAX_MEMBERS = 100;

    protected Team() {
    }

    public Team(String teamName, User captain, String university, TeamType teamType,
        TeamSkillLevel skillLevel, String description) {
        this.teamName = TeamName.of(teamName);
        this.captain = captain;
        this.university = UniversityName.of(university);
        this.teamType = teamType != null ? teamType : TeamType.OTHER;
        this.skillLevel = skillLevel != null ? skillLevel : TeamSkillLevel.AMATEUR;
        this.description = Description.of(description);
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

    public TeamSkillLevel getSkillLevel() {
        return skillLevel;
    }

    public Description getDescription() {
        return description;
    }

    public List<TeamMember> getMembers() {
        return members;
    }

    public void validateSameUniversity(User user) {
        if (!this.university.equals(user.getUniversity())) {
            throw new DifferentException(ErrorCode.DIFFERENT_UNIVERSITY);
        }
    }

    public void validateCanAcceptNewMember() {
        if (this.memberCount.count() >= MAX_MEMBERS) {
            throw new TeamCapacityExceededException();
        }
    }

    public void recruitMember(User user, TeamMemberRole role) {
        if (members.size() >= MAX_MEMBERS) {
            throw new TeamFullException("팀 정원이 초과되었습니다.");
        }

        if (members.stream().anyMatch(member -> member.getUser().equals(user))) {
            throw new DuplicatedException(ErrorCode.ALREADY_TEAM_MEMBER);
        }

        TeamMember teamMember = new TeamMember(this, user, role);
        this.members.add(teamMember);
        this.memberCount = this.memberCount.increase();
    }

    public void removeMember(TeamMember member) {
        if (this.memberCount.count() == 1) {
            throw new LastTeamMemberRemovalNotAllowedException();
        }
        members.remove(member);
        this.memberCount = this.memberCount.decrease();
    }

    public void changeTeamInfo(String teamName,
        String university,
        String skillLevel,
        String description) {

        this.teamName = TeamName.of(teamName);
        this.university = UniversityName.of(university);
        this.skillLevel = TeamSkillLevel.fromDisplayName(skillLevel);
        this.description = Description.of(description);
    }

    public boolean hasCaptain() {
        return members.stream()
            .anyMatch(TeamMember::isCaptain);
    }

    public boolean hasViceCaptain() {
        return members.stream()
            .anyMatch(TeamMember::isViceCaptain);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Team)) {
            return false;
        }
        Team team = (Team) o;
        return Objects.equals(getTeamId(), team.getTeamId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTeamId());
    }
}
