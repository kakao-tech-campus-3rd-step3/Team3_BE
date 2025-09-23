package com.shootdoori.match.exception;

public enum ErrorCode {
    ALREADY_TEAM_MEMBER("이미 해당 팀의 멤버입니다."),
    CAPTAIN_NOT_FOUND("팀장 정보가 없습니다."),
    DIFFERENT_UNIVERSITY("팀 소속 대학과 동일한 대학의 사용자만 가입할 수 있습니다."),
    DUPLICATED_USER("이미 존재하는 사용자입니다."),
    TEAM_CAPACITY_EXCEEDED("팀 정원이 가득 찼습니다. (최대 100명)"),
    TEAM_NOT_FOUND("해당 팀을 찾을 수 없습니다."),
    USER_NOT_FOUND("해당 유저를 찾을 수 없습니다."),
    TEAM_MEMBER_NOT_FOUND("해당 팀 멤버를 찾을 수 없습니다."),
    DUPLICATE_CAPTAIN("이미 팀에 회장이 존재합니다."),
    DUPLICATE_VICE_CAPTAIN("이미 팀에 부회장이 존재합니다."),
    LAST_TEAM_MEMBER_REMOVAL_NOT_ALLOWED("마지막 멤버는 제거할 수 없습니다."),
    INVALID_MEMBER_COUNT("멤버 수가 유효 범위를 벗어났습니다. (0~100명)"),

    JOIN_QUEUE_NOT_PENDING("대기중 상태의 신청만 처리할 수 있습니다."),
    JOIN_QUEUE_INVALID_TRANSITION("현재 상태에서 요청된 상태로 변경할 수 없습니다."),
    JOIN_QUEUE_ALREADY_PENDING("이미 대기중인 신청이 존재합니다."),
    JOIN_QUEUE_NOT_FOUND("해당 가입 신청을 찾을 수 없습니다."),

    NO_PERMISSION("허락되지 않은 요청입니다."),


    VENUE_NOT_FOUND("해당 경기장을 찾을 수 없습니다."),
    MATCH_REQUEST_NOT_FOUND("해당 매치 요청은 존재하지 않습니다."),
    MATCH_WAITING_NOT_FOUND("해당 매치 대기열은 존재하지 않습니다.");

    RECRUITMENT_NOT_FOUND("존재하지 않는 모집 공고입니다."),

    PROFILE_NOT_FOUND("해당 프로필을 찾을 수 없습니다.");


    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
