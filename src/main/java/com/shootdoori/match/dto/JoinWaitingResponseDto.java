package com.shootdoori.match.dto;

import java.time.LocalDateTime;

public record JoinWaitingResponseDto(Long id, Long teamId, Long applicantId, String status,
                                   String decisionReason, String decidedBy,
                                   LocalDateTime decidedAt) {

}
