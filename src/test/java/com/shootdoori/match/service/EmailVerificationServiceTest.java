package com.shootdoori.match.service;

import com.shootdoori.match.entity.EmailVerificationCode;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.UnauthorizedException;
import com.shootdoori.match.repository.EmailVerificationCodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceTest {
    @Mock private EmailVerificationCodeRepository codeRepository;
    @Mock private MailService mailService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private EmailVerificationService emailVerificationService;

    @Test
    @DisplayName("이메일 인증번호 전송 - 신규 이메일")
    void sendVerificationCode_newEmail() {
        // given
        String email = "new@univ.ac.kr";
        String encodedCode = "encoded-code";

        // `passwordEncoder.encode`가 어떤 문자열이든 받으면 `encodedCode`를 반환하도록 설정
        when(passwordEncoder.encode(anyString())).thenReturn(encodedCode);
        when(codeRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        emailVerificationService.sendVerificationCode(email);

        // then
        // ArgumentCaptor를 사용하여 codeRepository.save()에 전달된 EmailVerificationCode 객체를 캡처
        ArgumentCaptor<EmailVerificationCode> codeEntityCaptor = ArgumentCaptor.forClass(EmailVerificationCode.class);
        verify(codeRepository).save(codeEntityCaptor.capture());

        // 캡처된 객체의 인코딩된 코드가 우리가 예상한 값과 일치하는지 확인
        EmailVerificationCode savedEntity = codeEntityCaptor.getValue();
        assertThat(savedEntity.getEmail()).isEqualTo(email);
        assertThat(savedEntity.getCode()).isEqualTo(encodedCode);

        // ArgumentCaptor를 사용하여 mailService.sendEmail()에 전달된 'text' 인자를 캡처
        ArgumentCaptor<String> mailTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendEmail(eq(email), anyString(), mailTextCaptor.capture());

        // 메일 본문에 '인증번호: '라는 텍스트가 포함되어 있는지 확인 (랜덤 숫자 자체는 검증 불가)
        assertThat(mailTextCaptor.getValue()).contains("인증번호: ");
    }

    @Test
    @DisplayName("이메일 인증번호 전송 - 기존 이메일에 한 번 더 보내기(코드 업데이트)")
    void sendVerificationCode_existingEmail() {
        // given
        String email = "exist@univ.ac.kr";
        String newEncodedCode = "new-encoded-code";
        EmailVerificationCode existing = new EmailVerificationCode(email, "old-encoded-code");

        when(codeRepository.findByEmail(email)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(anyString())).thenReturn(newEncodedCode);

        // when
        emailVerificationService.sendVerificationCode(email);

        // then
        // save 메서드가 호출되었는지 확인
        ArgumentCaptor<EmailVerificationCode> codeEntityCaptor = ArgumentCaptor.forClass(EmailVerificationCode.class);
        verify(codeRepository).save(codeEntityCaptor.capture());

        // 캡처된 엔티티의 코드가 새로운 인코딩된 코드로 업데이트되었는지 확인
        EmailVerificationCode updatedEntity = codeEntityCaptor.getValue();
        assertThat(updatedEntity.getCode()).isEqualTo(newEncodedCode);

        // 메일 발송 여부 확인
        verify(mailService).sendEmail(eq(email), anyString(), anyString());
    }

    @Test
    @DisplayName("인증 성공 - 저장된 코드와 일치")
    void verifyCode_success() {
        // given
        String email = "test@univ.ac.kr";
        String rawCode = "123456";
        String encodedCode = "encoded-code";

        EmailVerificationCode savedCode = new EmailVerificationCode(email, encodedCode);

        when(codeRepository.findByEmail(email)).thenReturn(Optional.of(savedCode));
        when(passwordEncoder.matches(rawCode, encodedCode)).thenReturn(true);

        // when
        emailVerificationService.verifyCode(email, rawCode);

        // then
        verify(codeRepository).delete(savedCode);
    }

    @Test
    @DisplayName("인증 실패 - 코드 불일치")
    void verifyCode_invalidCode() {
        // given
        String email = "test@univ.ac.kr";
        String rawCode = "000000";
        String encodedCode = "encoded";

        EmailVerificationCode savedCode = new EmailVerificationCode(email, encodedCode);

        when(codeRepository.findByEmail(email)).thenReturn(Optional.of(savedCode));
        when(passwordEncoder.matches(rawCode, encodedCode)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyCode(email, rawCode))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining(ErrorCode.INVALID_OTP.getMessage());
    }

    @Test
    @DisplayName("인증 실패 - 해당 이메일 없음")
    void verifyCode_emailNotFound() {
        // given
        String email = "notfound@univ.ac.kr";

        when(codeRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyCode(email, "123456"))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining(ErrorCode.OTP_NOT_FOUND.getMessage());
    }
}
