package com.shootdoori.match.exception;

public enum ErrorCode {
    ALREADY_TEAM_MEMBER("이미 해당 팀의 멤버입니다."),
    CAPTAIN_NOT_FOUND("팀장 정보가 없습니다."),
    DIFFERENT_UNIVERSITY("팀 소속 대학과 동일한 대학의 사용자만 가입할 수 있습니다."),
    DUPLICATED_USER("이미 존재하는 사용자입니다."),
    TEAM_CAPACITY_EXCEEDED("팀 정원이 가득 찼습니다. (최대 100명)"),
    TEAM_NOT_FOUND("해당 팀을 찾을 수 없습니다."),
    USER_NOT_FOUND("해당 유저를 찾을 수 없습니다."),
    TEAM_MEMBER_NOT_FOUND("해당 팀 멤버를 찾을 수 없습니다.");


    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
