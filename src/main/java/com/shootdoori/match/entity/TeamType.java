package com.shootdoori.match.entity;

public enum TeamType {
    중앙동아리,
    과동아리,
    기타;

    public static TeamType from(String value) {
        return switch (value) {
            case "중앙동아리" -> TeamType.중앙동아리;
            case "과동아리" -> TeamType.과동아리;
            default -> TeamType.기타; // null 또는 매칭 실패 시 기본값
        };
    }
}
