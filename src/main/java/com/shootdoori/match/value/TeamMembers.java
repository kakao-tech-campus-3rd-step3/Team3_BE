package com.shootdoori.match.value;

import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.domain.team.LastTeamMemberRemovalNotAllowedException;
import com.shootdoori.match.exception.domain.team.TeamCapacityExceededException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
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

    @Embedded
    @AttributeOverride(name = "count", column = @Column(name = "MEMBER_COUNT", nullable = false))
    private MemberCount memberCount = MemberCount.of(0);

    protected TeamMembers() {
    }

    public TeamMembers(List<TeamMember> teamMembers) {
        this.teamMembers = teamMembers == null
            ? new ArrayList<>()
            : new ArrayList<>(teamMembers);
    }

    public List<TeamMember> getTeamMembers() {
        return teamMembers;
    }

    public MemberCount getMemberCount() {
        return memberCount;
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
        syncMemberCount();
    }

    public void removeMember(TeamMember targetMember) {
        ensureRemovable();
        teamMembers.remove(targetMember);
        syncMemberCount();
    }

    public void clear() {
        teamMembers.clear();
        syncMemberCount();
    }

    public void ensureNotFull() {
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
        if (teamMembers.stream().anyMatch(member -> member.isSameUser(targetUser))) {
            throw new DuplicatedException(ErrorCode.ALREADY_TEAM_MEMBER);
        }
    }

    private void syncMemberCount() {
        memberCount = memberCount.of(size());
    }
}
