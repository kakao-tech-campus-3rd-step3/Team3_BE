package com.shootdoori.match.entity.team;

import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.common.SoftDeleteTeamEntity;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.DifferentException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NoPermissionException;
import com.shootdoori.match.value.Description;
import com.shootdoori.match.value.MemberCount;
import com.shootdoori.match.value.TeamMembers;
import com.shootdoori.match.value.TeamName;
import com.shootdoori.match.value.UniversityName;
import jakarta.persistence.AttributeOverride;
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
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "team")
@SQLRestriction("status = 'ACTIVE'")
public class Team extends SoftDeleteTeamEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "SKILL_LEVEL", nullable = false, length = 20)
    private SkillLevel skillLevel = SkillLevel.AMATEUR;

    @Embedded
    @AttributeOverride(name = "description", column = @Column(name = "DESCRIPTION", length = 1000))
    private Description description;

    @Embedded
    private TeamMembers teamMembers = TeamMembers.empty();

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
        return teamMembers.getMemberCount();
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public Description getDescription() {
        return description;
    }

    public List<TeamMember> getTeamMembers() {
        return Collections.unmodifiableList(teamMembers.getTeamMembers());
    }

    public void validateSameUniversityAs(User user) {
        if (!this.university.equals(user.getUniversity())) {
            throw new DifferentException(ErrorCode.DIFFERENT_UNIVERSITY);
        }
    }

    public void ensureCapacityAvailable() {
        teamMembers.ensureNotFull();
    }

    public void addMember(User user, TeamMemberRole role) {
        TeamMember teamMember = new TeamMember(this, user, role);
        teamMembers.addMember(teamMember);
    }

    public void removeMember(TeamMember member) {
        teamMembers.removeMember(member);
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

    public boolean hasCaptain() {
        return teamMembers.hasCaptain();
    }

    public boolean hasViceCaptain() {
        return teamMembers.hasViceCaptain();
    }

    public void delete(Long userId) {
        if (!Objects.equals(getCaptain().getId(), userId)) {
            throw new NoPermissionException(ErrorCode.CAPTAIN_ONLY_OPERATION);
        }

        teamMembers.clear();
        changeStatusDeleted();
    }

    public void restore(Long userId) {
        if (!Objects.equals(getCaptain().getId(), userId)) {
            throw new NoPermissionException(ErrorCode.CAPTAIN_ONLY_OPERATION);
        }

        addMember(captain, TeamMemberRole.LEADER);
        changeStatusActive();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
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
