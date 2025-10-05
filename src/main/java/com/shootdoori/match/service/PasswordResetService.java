package com.shootdoori.match.service;

import com.shootdoori.match.entity.auth.PasswordOtpToken;
import com.shootdoori.match.entity.auth.PasswordResetToken;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.UnauthorizedException;
import com.shootdoori.match.repository.PasswordOtpTokenRepository;
import com.shootdoori.match.repository.PasswordResetTokenRepository;
import com.shootdoori.match.repository.ProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final PasswordOtpTokenRepository otpTokenRepository;
    private final PasswordResetTokenRepository resetTokenRepository;

    private static final int OTP_EXPIRATION_MINUTES = 3;
    private static final int RESET_TOKEN_EXPIRATION_MINUTES = 5;

    public PasswordResetService(ProfileRepository profileRepository, PasswordEncoder passwordEncoder, MailService mailService,
                                PasswordOtpTokenRepository otpTokenRepository, PasswordResetTokenRepository resetTokenRepository) {
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.otpTokenRepository = otpTokenRepository;
        this.resetTokenRepository = resetTokenRepository;
    }

    public void sendVerificationCode(String email) {
        User user = profileRepository.findByEmail(email).orElse(null);
        if (user == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return;
        }

        String rawCode = generateOtpCode();
        String encodedCode = passwordEncoder.encode(rawCode);

        PasswordOtpToken otpToken = otpTokenRepository.findByUser_Id(user.getId())
                    .map(existing -> {
                        existing.updateCode(encodedCode, OTP_EXPIRATION_MINUTES);
                return existing;
            })
            .orElseGet(() -> new PasswordOtpToken(user, encodedCode, OTP_EXPIRATION_MINUTES));

        otpTokenRepository.save(otpToken);

        String subject = "[슛도리] 비밀번호 재설정 인증번호 안내";
        String text = "인증번호: " + rawCode + "\n3분 안에 입력해주세요.";

        mailService.sendEmail(email, subject, text);
    }

    public String verifyCodeAndIssueToken(String email, String code) {
        PasswordOtpToken otpToken = otpTokenRepository.findByUser_Email(email)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.OTP_NOT_FOUND));
        otpToken.validateCode(code, passwordEncoder);

        User user = otpToken.getUser();
        String tempResetTokenValue = UUID.randomUUID().toString();

        PasswordResetToken resetToken = resetTokenRepository.findByUser_Id(user.getId())
            .map(existing -> {
                existing.updateToken(tempResetTokenValue, RESET_TOKEN_EXPIRATION_MINUTES);
                return existing;
            })
            .orElseGet(() -> new PasswordResetToken(user, tempResetTokenValue, RESET_TOKEN_EXPIRATION_MINUTES));

        resetTokenRepository.save(resetToken);
        otpTokenRepository.delete(otpToken);

        return tempResetTokenValue;
    }

    public void resetPasswordWithToken(String token, String newPassword) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
            .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_TOKEN));
        resetToken.validateExpiryDate();

        User user = resetToken.getUser();
        user.changePassword(passwordEncoder.encode(newPassword));
        profileRepository.save(user);

        resetTokenRepository.delete(resetToken);
    }

    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1000000));
    }
}