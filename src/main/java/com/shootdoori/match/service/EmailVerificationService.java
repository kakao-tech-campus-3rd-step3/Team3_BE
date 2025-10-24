package com.shootdoori.match.service;

import com.shootdoori.match.config.PasswordEncoderProvider;
import com.shootdoori.match.entity.auth.EmailVerificationCode;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.UnauthorizedException;
import com.shootdoori.match.repository.EmailVerificationCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@Transactional
public class EmailVerificationService {

    private final EmailVerificationCodeRepository codeRepository;
    private final MailService mailService;
    private final PasswordEncoderProvider passwordEncoder;

    public EmailVerificationService(EmailVerificationCodeRepository codeRepository,
                                    MailService mailService,
                                    PasswordEncoderProvider  passwordEncoder) {
        this.codeRepository = codeRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void sendVerificationCode(String email) {
        String rawCode = generateOtpCode();
        String encodedCode = passwordEncoder.encode(rawCode);

        EmailVerificationCode codeEntity = codeRepository.findByEmail(email)
            .map(existing -> {
                existing.updateCode(encodedCode);
                return existing;
            })
            .orElseGet(() -> new EmailVerificationCode(email, encodedCode));

        codeRepository.save(codeEntity);

        String subject = "[슛도리] 이메일 인증번호 안내";
        String text = "인증번호: " + rawCode + "\n회원가입 화면에서 입력해주세요.";

        mailService.sendEmail(email, subject, text);
    }

    public void verifyCode(String email, String code) {
        EmailVerificationCode codeEntity = codeRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException(ErrorCode.OTP_NOT_FOUND));

        codeEntity.validateCode(code, passwordEncoder.getEncoder());

        codeRepository.delete(codeEntity);
    }

    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1000000));
    }
}