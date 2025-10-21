package com.shootdoori.match.entity.team;

import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.domain.team.LastTeamMemberRemovalNotAllowedException;
import com.shootdoori.match.exception.domain.team.TeamCapacityExceededException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class TeamMembers {

    private static final int MAX_TEAM_MEMBERS = 100;

    @OneToMany(
        mappedBy = "team",
        cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
        orphanRemoval = true
    )
    private List<TeamMember> teamMembers = new ArrayList<>();

    protected TeamMembers() {}

    public TeamMembers(List<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public List<TeamMember> getTeamMembers() {
        return teamMembers;
    }

    public static TeamMembers empty() {
        return new TeamMembers();
    }

    public int size() {
        return teamMembers.size();
    }

    public boolean isEmpty() {
        return teamMembers.isEmpty();
    }

    public boolean hasCaptain() {
        return teamMembers.stream().anyMatch(TeamMember::isCaptain);
    }

    public boolean hasViceCaptain() {
        return teamMembers.stream().anyMatch(TeamMember::isViceCaptain);
    }

    public void addMember(TeamMember targetMember) {
        User targetUser = targetMember.getUser();

        ensureNotFull();
        ensureNotMember(targetUser);
        teamMembers.add(targetMember);
    }

    public void removeMember(TeamMember targetMember) {
        ensureRemovable();
        teamMembers.remove(targetMember);
    }

    public void clear() {
        teamMembers.clear();
    }

    private void ensureNotFull() {
        if (size() >= MAX_TEAM_MEMBERS) {
            throw new TeamCapacityExceededException();
        }
    }

    private void ensureRemovable() {
        if (size() <= 1) {
            throw new LastTeamMemberRemovalNotAllowedException();
        }
    }

    private void ensureNotMember(User targetUser) {
        if (teamMembers.stream().anyMatch(member -> member.getUser().equals(targetUser))) {
            throw new DuplicatedException(ErrorCode.ALREADY_TEAM_MEMBER);
        }
    }
}
