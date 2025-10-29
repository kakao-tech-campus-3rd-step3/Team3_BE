package com.shootdoori.match.controller;

import com.shootdoori.match.dto.*;
import com.shootdoori.match.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private static final long SECONDS_PER_MINUTE = 60L;

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/send-code")
    public ResponseEntity<PasswordResetResponse> sendVerificationCode(@Valid @RequestBody SendCodeRequest request) {
        long exp = passwordResetService.sendVerificationCode(request.email());
        return new ResponseEntity<>(new PasswordResetResponse("인증번호가 이메일로 발송되었습니다.", exp * SECONDS_PER_MINUTE),
                HttpStatus.OK);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<PasswordTokenResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        String token = passwordResetService.verifyCodeAndIssueToken(request.email(), request.code());
        return new ResponseEntity<>(new PasswordTokenResponse(token), HttpStatus.OK);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PasswordResetResponse> confirmPasswordReset(@Valid @RequestBody ResetPasswordRequest request) {
        long exp = passwordResetService.resetPasswordWithToken(request.token(), request.password());
        return new ResponseEntity<>(new PasswordResetResponse("비밀번호가 성공적으로 변경되었습니다.", exp * SECONDS_PER_MINUTE),
                HttpStatus.OK);
    }
}