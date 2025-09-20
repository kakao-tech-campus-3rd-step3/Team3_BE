package com.shootdoori.match.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "이메일을 반드시 입력해야 합니다.")
    @Size(max = 50, message = "이메일은 최대 50자까지 입력 가능합니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.") String email,

    @NotBlank(message = "비밀번호를 반드시 입력해야 합니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
        message = "비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 모두 포함해야 합니다."
    )
    String password
) {
}