package com.shootdoori.match.value;

import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.domain.team.LastTeamMemberRemovalNotAllowedException;
import com.shootdoori.match.exception.domain.team.TeamCapacityExceededException;
import com.shootdoori.match.exception.domain.team.TeamHasRemainingMembersException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class TeamMembers {

    private static final int MIN_ACTIVE_MEMBERS = 1;
    private static final int MAX_TEAM_MEMBERS = 100;

    @OneToMany(
        mappedBy = "team",
        cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
        orphanRemoval = true
    )
    private List<TeamMember> members = new ArrayList<>();

    protected TeamMembers() {
    }

    public TeamMembers(List<TeamMember> members) {
        this.members = members == null
            ? new ArrayList<>()
            : new ArrayList<>(members);
    }

    public List<TeamMember> getTeamMembers() {
        return members;
    }

    public static TeamMembers empty() {
        return new TeamMembers();
    }

    public int size() {
        return members.size();
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public boolean hasCaptain() {
        return members.stream().anyMatch(TeamMember::isCaptain);
    }

    public boolean hasViceCaptain() {
        return members.stream().anyMatch(TeamMember::isViceCaptain);
    }

    public void addMember(TeamMember targetMember) {
        User targetUser = targetMember.getUser();

        ensureNotFull();
        ensureNotMember(targetUser);
        members.add(targetMember);
    }

    public void removeMember(TeamMember targetMember) {
        ensureRemovable();
        members.remove(targetMember);
    }

    public void clear() {
        ensureNoMembersRemaining();
        members.clear();
    }

    public void ensureNotFull() {
        if (isFull()) {
            throw new TeamCapacityExceededException();
        }
    }

    private void ensureRemovable() {
        if (isBelowMinActive()) {
            throw new LastTeamMemberRemovalNotAllowedException();
        }
    }

    private void ensureNotMember(User targetUser) {
        if (members.stream().anyMatch(member -> member.isSameUser(targetUser))) {
            throw new DuplicatedException(ErrorCode.ALREADY_TEAM_MEMBER);
        }
    }

    private void ensureNoMembersRemaining() {
        if (isRemaining()) {
            throw new TeamHasRemainingMembersException();
        }
    }

    private boolean isFull() {
        return size() >= MAX_TEAM_MEMBERS;
    }

    private boolean isBelowMinActive() {
        return size() <= MIN_ACTIVE_MEMBERS;
    }

    private boolean isRemaining() {
        return size() > MIN_ACTIVE_MEMBERS;
    }
}
