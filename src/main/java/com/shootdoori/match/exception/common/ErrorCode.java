package com.shootdoori.match.exception.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // User
    USER_NOT_FOUND("해당 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATED_USER("이미 존재하는 사용자입니다.", HttpStatus.CONFLICT),
    USER_ALREADY_ACTIVE("유저가 이미 활성화 상태입니다.", HttpStatus.CONFLICT),
    USER_ALREADY_DELETED("유저가 이미 삭제되었습니다", HttpStatus.CONFLICT),
    INVALID_EMAIL("유효하지 않은 이메일입니다.", HttpStatus.CONFLICT),

    // Team
    TEAM_NOT_FOUND("해당 팀을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TEAM_ALREADY_ACTIVE("팀이 이미 활성화 상태입니다.", HttpStatus.CONFLICT),
    TEAM_ALREADY_DELETED("팀이 이미 삭제 상태입니다.", HttpStatus.CONFLICT),
    TEAM_CAPACITY_EXCEEDED("팀 정원이 가득 찼습니다. (최대 100명)", HttpStatus.CONFLICT),
    DUPLICATE_ROLE("이미 팀에 해당 역할이 존재합니다", HttpStatus.CONFLICT),
    LAST_TEAM_MEMBER_REMOVAL_NOT_ALLOWED("마지막 멤버는 제거할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_MEMBER_COUNT("멤버 수가 유효 범위를 벗어났습니다. (0~100명)", HttpStatus.BAD_REQUEST),
    LEADER_CANNOT_LEAVE_TEAM("회장은 팀 탈퇴를 할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CAPTAIN_NOT_FOUND("팀장 정보가 없습니다.", HttpStatus.NOT_FOUND),
    DIFFERENT_UNIVERSITY("팀 소속 대학과 동일한 대학의 사용자만 가입할 수 있습니다.", HttpStatus.FORBIDDEN),
    TEAM_REVIEW_NOT_FOUND("해당 팀 리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // Team Member
    ALREADY_TEAM_MEMBER("이미 해당 팀의 멤버입니다.", HttpStatus.CONFLICT),
    ALREADY_OTHER_TEAM_MEMBER("이미 다른 팀에 소속되어 있습니다.", HttpStatus.CONFLICT),
    TEAM_MEMBER_NOT_FOUND("해당 팀 멤버를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MERCENARY_REVIEW_NOT_FOUND("해당 용병 리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // Delegation
    SELF_DELEGATION_NOT_ALLOWED("자신에게 역할을 위임할 수 없습니다.", HttpStatus.BAD_REQUEST),
    LEADERSHIP_DELEGATION_FORBIDDEN("회장만 회장직을 위임할 수 있습니다.", HttpStatus.FORBIDDEN),
    VICE_LEADERSHIP_DELEGATION_FORBIDDEN("부회장만 부회장직을 위임할 수 있습니다.", HttpStatus.FORBIDDEN),
    DIFFERENT_TEAM_DELEGATION_NOT_ALLOWED("다른 팀 멤버에게는 역할을 위임할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // Join Waiting
    JOIN_WAITING_NOT_PENDING("대기중 상태의 신청만 처리할 수 있습니다.", HttpStatus.BAD_REQUEST),
    JOIN_WAITING_INVALID_TRANSITION("현재 상태에서 요청된 상태로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST),
    JOIN_WAITING_ALREADY_PENDING("이미 대기중인 신청이 존재합니다.", HttpStatus.CONFLICT),
    JOIN_WAITING_NOT_FOUND("해당 가입 신청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // Match
    MATCH_NOT_FOUND("해당 매치를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    VENUE_NOT_FOUND("해당 경기장을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MATCH_REQUEST_NOT_FOUND("해당 매치 요청은 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    MATCH_WAITING_NOT_FOUND("해당 매치 대기열은 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    ALREADY_MATCH_REQUEST("이미 해당 매치에 대해 요청한 상태이거나 수락/거절 되었습니다.",HttpStatus.BAD_REQUEST),
    ONESELF_MATCH("자기 자신 팀으로의 매치는 불가능합니다.", HttpStatus.BAD_REQUEST),

    // Recruitment
    RECRUITMENT_NOT_FOUND("존재하지 않는 모집 공고입니다.", HttpStatus.NOT_FOUND),

    // Profile
    PROFILE_NOT_FOUND("해당 프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // Auth / Permission
    NO_PERMISSION("허락되지 않은 요청입니다.", HttpStatus.FORBIDDEN),
    CAPTAIN_ONLY_OPERATION("팀장만 수행할 수 있는 작업입니다.", HttpStatus.FORBIDDEN),
    LEADER_CANNOT_LEAVE("팀장은 팀을 떠날 수 없습니다.", HttpStatus.FORBIDDEN),
    INSUFFICIENT_ROLE_FOR_KICK("해당 멤버를 추방할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INSUFFICIENT_ROLE_FOR_JOIN_DECISION("가입 승인/거부 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INSUFFICIENT_ROLE_FOR_ROLE_CHANGE("역할 변경 권한이 없습니다.", HttpStatus.FORBIDDEN),
    MATCH_REQUEST_OWNERSHIP_VIOLATION("자신의 팀 매치 요청만 취소할 수 있습니다.", HttpStatus.FORBIDDEN),
    MATCH_WAITING_OWNERSHIP_VIOLATION("자신의 팀 매치 대기만 처리할 수 있습니다.", HttpStatus.FORBIDDEN),
    MATCH_OPERATION_PERMISSION_DENIED("매치 관련 작업 권한이 없습니다.", HttpStatus.FORBIDDEN),
    JOIN_REQUEST_OWNERSHIP_VIOLATION("자신의 가입 신청만 취소할 수 있습니다.", HttpStatus.FORBIDDEN),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    OTP_NOT_FOUND("유효한 인증번호가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    INVALID_OTP("인증번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),

    // Review
    MATCH_NOT_FINISHED_YET("아직 경기가 종료되지 않아 리뷰를 작성할 수 없습니다.", HttpStatus.BAD_REQUEST);


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
