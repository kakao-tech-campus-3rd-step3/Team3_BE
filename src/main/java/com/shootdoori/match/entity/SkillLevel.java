package com.shootdoori.match.entity;

public enum SkillLevel {
    프로,
    세미프로,
    아마추어;

    public static SkillLevel from(String value) {
        return switch (value) {
            case "프로" -> SkillLevel.프로;
            case "세미프로" -> SkillLevel.세미프로;
            default -> SkillLevel.아마추어; // null 또는 매칭 실패 시 기본값
        };
    }
}
