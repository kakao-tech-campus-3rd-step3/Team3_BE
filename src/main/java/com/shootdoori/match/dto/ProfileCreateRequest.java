package com.shootdoori.match.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileCreateRequest(
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(min = 2, max = 100, message = "이름은 2자 이상 100자 이하로 입력해주세요.")
    String name,

    @NotBlank(message = "스킬 레벨은 필수 입력 값입니다.")
    String skillLevel,

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일 주소는 255자를 초과할 수 없습니다.")
    String email,

    @NotBlank(message = "학교 이메일은 필수 입력 값입니다.")
    @Email(message = "학교 이메일 형식이 올바르지 않습니다.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9-]+\\.)*ac\\.kr$", message = "학교 이메일은 'ac.kr' 도메인이어야 합니다.")
    @Size(max = 255, message = "학교 이메일 주소는 255자를 초과할 수 없습니다.")
    String universityEmail,

    @NotBlank(message = "핸드폰 번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "핸드폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    String phoneNumber,

    @NotBlank(message = "대학교 이름은 필수 입력 값입니다.")
    @Size(max = 100, message = "대학교 이름은 100자를 초과할 수 없습니다.")
    String university,

    @NotBlank(message = "학과 이름은 필수 입력 값입니다.")
    @Size(max = 100, message = "학과 이름은 100자를 초과할 수 없습니다.")
    String department,

    @NotBlank(message = "학번은 필수 입력 값입니다.")
    String studentYear,

    @Size(max = 500, message = "자기소개는 500자를 초과할 수 없습니다.")
    String bio
) {
}
