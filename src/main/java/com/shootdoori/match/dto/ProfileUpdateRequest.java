package com.shootdoori.match.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
    @Size(min = 2, max = 100, message = "이름은 2자 이상 100자 이하로 입력해주세요.")
    String name
) {
}
