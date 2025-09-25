package com.shootdoori.match.dto;

public record JoinQueueApproveRequestDto(
    Long approverId, // TeamMemberId
    String role,
    String decisionReason
) {

}
