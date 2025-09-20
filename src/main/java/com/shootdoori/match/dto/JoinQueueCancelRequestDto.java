package com.shootdoori.match.dto;

public record JoinQueueCancelRequestDto(
    Long requesterId, // UserId
    String decisionReason
) {

}
