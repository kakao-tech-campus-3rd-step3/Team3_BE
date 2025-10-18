package com.shootdoori.match.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyCodeRequest(
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.ac\\.kr$",
        message = "대학교 이메일 형식만 가능합니다. (.ac.kr)"
    )
    String email,

    @NotBlank(message = "인증번호는 필수 입력값입니다.")
    String code
) {

}