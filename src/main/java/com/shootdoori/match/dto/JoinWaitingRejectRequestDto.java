package com.shootdoori.match.dto;

public record JoinWaitingRejectRequestDto(
    Long approverId, // TeamMemberId
    String reason
) {

}
