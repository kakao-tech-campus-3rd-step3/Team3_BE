package com.shootdoori.match.value;

import jakarta.persistence.Embeddable;

@Embeddable
public record MemberCount(int count) {

    private static final int MIN_MEMBERS = 1;
    private static final int MAX_MEMBERS = 100;

    public MemberCount {
        if (count < MIN_MEMBERS || count > MAX_MEMBERS) {
            throw new IllegalArgumentException("멤버 수는 1~100명입니다.");
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
