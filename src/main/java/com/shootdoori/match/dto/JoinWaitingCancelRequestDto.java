package com.shootdoori.match.dto;

public record JoinWaitingCancelRequestDto(
    Long requesterId, // UserId
    String decisionReason
) {

}
