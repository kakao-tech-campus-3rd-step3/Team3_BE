package com.shootdoori.match.value;

import com.shootdoori.match.exception.domain.team.InvalidMemberCountException;
import jakarta.persistence.Embeddable;

@Embeddable
public record MemberCount(int count) {

    private static final int MIN_TEAM_MEMBERS = 0;
    private static final int MAX_TEAM_MEMBERS = 100;

    public MemberCount {
        if (count < MIN_TEAM_MEMBERS || count > MAX_TEAM_MEMBERS) {
            throw new InvalidMemberCountException(count);
        }
    }

    public static MemberCount of(int count) {
        return new MemberCount(count);
    }
}
