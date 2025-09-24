package com.shootdoori.match.dto;

public record JoinWaitingApproveRequestDto(
    Long approverId, // TeamMemberId
    String role,
    String decisionReason
) {

}
