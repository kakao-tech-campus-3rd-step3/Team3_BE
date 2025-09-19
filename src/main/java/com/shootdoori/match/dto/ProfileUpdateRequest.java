package com.shootdoori.match.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
    @Size(min = 2, max = 100, message = "이름은 2자 이상 100자 이하로 입력해주세요.")
    String name,

    @Size(max = 4, message = "스킬 레벨은 4자 이하로 입력해주세요.")
    String skillLevel,

    @Size(max = 10, message = "포지션은 10자 이하로 입력해주세요.")
    String position,

    @Size(max = 500, message = "500자 이하로 입력해주세요.")
    String bio
) {
}