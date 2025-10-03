package com.shootdoori.match.value;

import com.shootdoori.match.exception.domain.team.InvalidMemberCountException;
import jakarta.persistence.Embeddable;

@Embeddable
public record MemberCount(int count) {

    private static final int MIN_MEMBERS = 0;
    private static final int MAX_MEMBERS = 100;

    public MemberCount {
        if (count < MIN_MEMBERS || count > MAX_MEMBERS) {
            throw new InvalidMemberCountException(count);
        }
    }

    public static MemberCount of(int count) {
        return new MemberCount(count);
    }

    public MemberCount increase() {
        return new MemberCount(count + 1);
    }

    public MemberCount decrease() {
        return new MemberCount(count - 1);
    }
}
