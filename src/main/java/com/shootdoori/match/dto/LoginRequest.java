package com.shootdoori.match.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "이메일을 반드시 입력해야 합니다.")
    @Size(max = 50, message = "이메일은 최대 50자까지 입력 가능합니다.")
    @Email(message = "이메일 형식이 아닙니다.")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9-]+\\.)*ac\\.kr$",
        message = "학교 이메일 주소만 사용할 수 있습니다. (예: example@univ.ac.kr)"
    )
    @Size(max = 255, message = "이메일 주소는 255자를 초과할 수 없습니다.")
    String email,

    @NotBlank(message = "비밀번호를 반드시 입력해야 합니다.")
    @Size(min = 8, max = 20)
    String password
) {
}