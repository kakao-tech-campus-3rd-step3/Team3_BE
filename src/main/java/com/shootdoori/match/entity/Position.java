package com.shootdoori.match.entity;

public enum Position {

    GK("골키퍼"),
    DF("수비수"),
    MF("미드필더"),
    FW("공격수"),

    // 수비
    CB("센터백"),
    FB("풀백"),
    WB("윙백"),

    CM("중앙 미드필더"),
    DM("수비형 미드필더"),
    AM("공격형 미드필더"),
    LM("측면 미드필더(좌)"),
    RM("측면 미드필더(우)"),

    CF("중앙 공격수"),
    SS("세컨드 스트라이커"),
    LW("윙어(좌)"),
    RW("윙어(우)");

    private final String displayName;

    Position(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Position fromDisplayName(String displayName) {
        for (Position p : values()) {
            if (p.displayName.equals(displayName)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown position: " + displayName);
    }

    public static Position fromCode(String code) {
        try {
            return Position.valueOf(code.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown position code: " + code);
        }
    }
}