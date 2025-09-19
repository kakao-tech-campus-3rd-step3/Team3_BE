package com.shootdoori.match.dto;

public record JoinQueueRejectRequestDto(
    Long approverId, // TeamMemberId
    String reason
) {

}
