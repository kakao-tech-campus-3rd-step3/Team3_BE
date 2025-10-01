package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    ALREADY_TEAM_MEMBER("이미 해당 팀의 멤버입니다.", HttpStatus.CONFLICT),
    CAPTAIN_NOT_FOUND("팀장 정보가 없습니다.", HttpStatus.NOT_FOUND),
    DIFFERENT_UNIVERSITY("팀 소속 대학과 동일한 대학의 사용자만 가입할 수 있습니다.", HttpStatus.FORBIDDEN),
    DUPLICATED_USER("이미 존재하는 사용자입니다.", HttpStatus.CONFLICT),
    TEAM_CAPACITY_EXCEEDED("팀 정원이 가득 찼습니다. (최대 100명)", HttpStatus.CONFLICT),
    TEAM_NOT_FOUND("해당 팀을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("해당 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TEAM_MEMBER_NOT_FOUND("해당 팀 멤버를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_ROLE("이미 팀에 해당 역할이 존재합니다", HttpStatus.CONFLICT),
    LAST_TEAM_MEMBER_REMOVAL_NOT_ALLOWED("마지막 멤버는 제거할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_MEMBER_COUNT("멤버 수가 유효 범위를 벗어났습니다. (0~100명)", HttpStatus.BAD_REQUEST),

    SELF_DELEGATION_NOT_ALLOWED("자신에게 역할을 위임할 수 없습니다.", HttpStatus.BAD_REQUEST),
    LEADERSHIP_DELEGATION_FORBIDDEN("회장만 회장직을 위임할 수 있습니다.", HttpStatus.FORBIDDEN),
    VICE_LEADERSHIP_DELEGATION_FORBIDDEN("회장 또는 부회장만 부회장직을 위임할 수 있습니다.", HttpStatus.FORBIDDEN),
    DIFFERENT_TEAM_DELEGATION_NOT_ALLOWED("다른 팀 멤버에게는 역할을 위임할 수 없습니다.", HttpStatus.BAD_REQUEST),

    JOIN_WAITING_NOT_PENDING("대기중 상태의 신청만 처리할 수 있습니다.", HttpStatus.BAD_REQUEST),
    JOIN_WAITING_INVALID_TRANSITION("현재 상태에서 요청된 상태로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST),
    JOIN_WAITING_ALREADY_PENDING("이미 대기중인 신청이 존재합니다.", HttpStatus.CONFLICT),
    JOIN_WAITING_NOT_FOUND("해당 가입 신청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    NO_PERMISSION("허락되지 않은 요청입니다.", HttpStatus.FORBIDDEN),

    VENUE_NOT_FOUND("해당 경기장을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MATCH_REQUEST_NOT_FOUND("해당 매치 요청은 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    MATCH_WAITING_NOT_FOUND("해당 매치 대기열은 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    RECRUITMENT_NOT_FOUND("존재하지 않는 모집 공고입니다.", HttpStatus.NOT_FOUND),

    PROFILE_NOT_FOUND("해당 프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    ONESELF_MATCH("자기 자신 팀으로의 매치는 불가능합니다.", HttpStatus.BAD_REQUEST),

    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    OTP_NOT_FOUND("유효한 인증번호가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    INVALID_OTP("인증번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);


  private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
