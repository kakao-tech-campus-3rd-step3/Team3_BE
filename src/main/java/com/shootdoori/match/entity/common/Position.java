package com.shootdoori.match.entity.common;

public enum Position {

    GK,
    DF,
    MF,
    FW,

    CB,
    LB,
    RB,

    DM,
    CM,
    AM,

    ST,
    LW,
    RW;

    public static Position fromCode(String code) {
        try {
            return Position.valueOf(code.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown position code: " + code);
        }
    }
}
