package com.shootdoori.match.service;

import com.shootdoori.match.entity.auth.PasswordOtpToken;
import com.shootdoori.match.entity.auth.PasswordResetToken;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.exception.common.TooManyRequestsException;
import com.shootdoori.match.exception.common.UnauthorizedException;
import com.shootdoori.match.repository.PasswordOtpTokenRepository;
import com.shootdoori.match.repository.PasswordResetTokenRepository;
import com.shootdoori.match.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MailService mailService;
    @Mock private PasswordOtpTokenRepository otpTokenRepository;
    @Mock private PasswordResetTokenRepository resetTokenRepository;

    @InjectMocks private PasswordResetService passwordResetService;

    private User testUser;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testUser = mock(User.class);
        lenient().when(testUser.getId()).thenReturn(1L);
    }

    @Test
    @DisplayName("인증번호 최초 발송 성공 - 새 토큰 생성")
    void sendVerificationCode_FirstTime_Success() {
        // given
        when(profileRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedCode");
        when(otpTokenRepository.findByUser_Id(testUser.getId())).thenReturn(Optional.empty());

        // when
        passwordResetService.sendVerificationCode(testEmail);

        // then
        verify(profileRepository).findByEmail(testEmail);
        verify(otpTokenRepository).findByUser_Id(testUser.getId());
        verify(otpTokenRepository).save(any(PasswordOtpToken.class));

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(mailService).sendEmail(emailCaptor.capture(), subjectCaptor.capture(), textCaptor.capture());

        assertThat(emailCaptor.getValue()).isEqualTo(testEmail);
        assertThat(subjectCaptor.getValue()).contains("비밀번호 재설정");
        assertThat(textCaptor.getValue()).contains("인증번호:");
    }

    @Test
    @DisplayName("인증번호 재발송 - 기존 토큰 업데이트")
    void sendVerificationCode_SecondTime_UpdateExisting() {
        // given
        PasswordOtpToken existingToken = mock(PasswordOtpToken.class);

        when(profileRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedCode");
        when(otpTokenRepository.findByUser_Id(testUser.getId())).thenReturn(Optional.of(existingToken));

        // when
        passwordResetService.sendVerificationCode(testEmail);

        // then
        verify(otpTokenRepository).findByUser_Id(testUser.getId());
        verify(existingToken).incrementRequestCount();
        verify(existingToken).updateCode("newEncodedCode", 3);
        verify(otpTokenRepository).save(existingToken);
        verify(mailService).sendEmail(eq(testEmail), anyString(), anyString());
    }

    @Test
    @DisplayName("인증번호 연속 두 번 발송 - 업데이트 두 번 수행")
    void sendVerificationCode_TwiceInARow_UpdatesTwice() {
        // given
        PasswordOtpToken existingToken = mock(PasswordOtpToken.class);

        when(profileRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedCode1", "encodedCode2");

        // 첫 번째 호출: 새 토큰 생성
        when(otpTokenRepository.findByUser_Id(testUser.getId())).thenReturn(Optional.empty());

        // 첫 번째 발송
        passwordResetService.sendVerificationCode(testEmail);

        // 두 번째 호출: 기존 토큰 업데이트
        when(otpTokenRepository.findByUser_Id(testUser.getId())).thenReturn(Optional.of(existingToken));

        // when - 두 번째 발송
        passwordResetService.sendVerificationCode(testEmail);

        // then
        verify(otpTokenRepository, times(2)).findByUser_Id(testUser.getId());
        verify(otpTokenRepository, times(2)).save(any(PasswordOtpToken.class));
        verify(existingToken).incrementRequestCount();
        verify(existingToken).updateCode("encodedCode2", 3);
        verify(mailService, times(2)).sendEmail(eq(testEmail), anyString(), anyString());
    }

    @Test
    @DisplayName("인증번호 발송 - 하루 5회 초과 시 TooManyRequestsException 발생")
    void sendVerificationCode_TooManyRequests() {
        // given
        PasswordOtpToken existingToken = mock(PasswordOtpToken.class);

        when(profileRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedCode");
        when(otpTokenRepository.findByUser_Id(testUser.getId())).thenReturn(Optional.of(existingToken));

        doThrow(new TooManyRequestsException(ErrorCode.LIMITED_OTP_REQUESTS))
            .when(existingToken).incrementRequestCount();

        // when & then
        assertThatExceptionOfType(TooManyRequestsException.class)
            .isThrownBy(() -> passwordResetService.sendVerificationCode(testEmail));

        verify(existingToken).incrementRequestCount();
        verify(existingToken, never()).updateCode(anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("인증번호 발송 - 하루 5회까지 발송 가능, 초과 시 TooManyRequestsException 발생")
    void incrementRequestCount_TooManyRequests() {
        // given
        User mockUser = createUser();
        PasswordOtpToken otpToken = new PasswordOtpToken(mockUser, "encodedCode", 3);

        // when & then
        for (int i = 1; i <= 5; i++) {
            assertThatCode(otpToken::incrementRequestCount)
                .doesNotThrowAnyException();
        }

        assertThatThrownBy(otpToken::incrementRequestCount)
            .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 인증번호 발송 시 예외 발생")
    void sendVerificationCode_UserNotFound() {
        // given
        when(profileRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(NotFoundException.class)
            .isThrownBy(() -> passwordResetService.sendVerificationCode(testEmail));

        verify(otpTokenRepository, never()).save(any());
        verify(mailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("인증번호 검증 및 토큰 최초 발급 성공")
    void verifyCodeAndIssueToken_FirstTime_Success() {
        // given
        String code = "123456";
        PasswordOtpToken otpToken = mock(PasswordOtpToken.class);

        when(otpTokenRepository.findByUser_Email(testEmail)).thenReturn(Optional.of(otpToken));
        when(otpToken.getUser()).thenReturn(testUser);
        when(resetTokenRepository.findByUser_Id(testUser.getId())).thenReturn(Optional.empty());
        doNothing().when(otpToken).validateCode(code, passwordEncoder);

        // when
        String token = passwordResetService.verifyCodeAndIssueToken(testEmail, code);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        verify(otpToken).validateCode(code, passwordEncoder);
        verify(resetTokenRepository).findByUser_Id(testUser.getId());
        verify(resetTokenRepository).save(any(PasswordResetToken.class));
        verify(otpTokenRepository).delete(otpToken);
    }

    @Test
    @DisplayName("인증번호 재검증 - 기존 리셋 토큰 업데이트")
    void verifyCodeAndIssueToken_SecondTime_UpdateExisting() {
        // given
        String code = "123456";
        PasswordOtpToken otpToken = mock(PasswordOtpToken.class);
        PasswordResetToken existingResetToken = mock(PasswordResetToken.class);

        when(otpTokenRepository.findByUser_Email(testEmail)).thenReturn(Optional.of(otpToken));
        when(otpToken.getUser()).thenReturn(testUser);
        when(resetTokenRepository.findByUser_Id(testUser.getId())).thenReturn(Optional.of(existingResetToken));
        doNothing().when(otpToken).validateCode(code, passwordEncoder);

        // when
        String token = passwordResetService.verifyCodeAndIssueToken(testEmail, code);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        verify(otpToken).validateCode(code, passwordEncoder);
        verify(resetTokenRepository).findByUser_Id(testUser.getId());
        verify(existingResetToken).updateToken(anyString(), eq(5));
        verify(resetTokenRepository).save(existingResetToken);
        verify(otpTokenRepository).delete(otpToken);
    }

    @Test
    @DisplayName("인증번호 연속 두 번 검증 - 리셋 토큰 업데이트 두 번 수행")
    void verifyCodeAndIssueToken_TwiceInARow_UpdatesTwice() {
        // given
        String code = "123456";
        PasswordOtpToken otpToken1 = mock(PasswordOtpToken.class);
        PasswordOtpToken otpToken2 = mock(PasswordOtpToken.class);
        PasswordResetToken existingResetToken = mock(PasswordResetToken.class);

        when(otpToken1.getUser()).thenReturn(testUser);
        when(otpToken2.getUser()).thenReturn(testUser);

        // 첫 번째와 두 번째 검증 설정
        when(otpTokenRepository.findByUser_Email(testEmail))
            .thenReturn(Optional.of(otpToken1))
            .thenReturn(Optional.of(otpToken2));

        when(resetTokenRepository.findByUser_Id(testUser.getId()))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingResetToken));

        doNothing().when(otpToken1).validateCode(code, passwordEncoder);
        doNothing().when(otpToken2).validateCode(code, passwordEncoder);

        // when
        String firstToken = passwordResetService.verifyCodeAndIssueToken(testEmail, code);
        String secondToken = passwordResetService.verifyCodeAndIssueToken(testEmail, code);

        // then
        assertThat(firstToken).isNotNull();
        assertThat(secondToken).isNotNull();
        assertThat(firstToken).isNotEqualTo(secondToken);

        verify(resetTokenRepository, times(2)).findByUser_Id(testUser.getId());
        verify(resetTokenRepository, times(2)).save(any(PasswordResetToken.class));
        verify(existingResetToken).updateToken(anyString(), eq(5));
        verify(otpTokenRepository, times(2)).delete(any(PasswordOtpToken.class));
    }

    @Test
    @DisplayName("존재하지 않는 OTP 토큰으로 검증 시 예외 발생")
    void verifyCodeAndIssueToken_OtpTokenNotFound() {
        // given
        String code = "123456";
        when(otpTokenRepository.findByUser_Email(testEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> passwordResetService.verifyCodeAndIssueToken(testEmail, code))
            .isInstanceOf(UnauthorizedException.class);

        verify(resetTokenRepository, never()).save(any());
        verify(otpTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("잘못된 인증번호로 검증 시 예외 발생")
    void verifyCodeAndIssueToken_InvalidCode() {
        // given
        String code = "wrong";
        PasswordOtpToken otpToken = mock(PasswordOtpToken.class);

        when(otpTokenRepository.findByUser_Email(testEmail)).thenReturn(Optional.of(otpToken));
        doThrow(new UnauthorizedException("인증번호가 일치하지 않습니다."))
            .when(otpToken).validateCode(code, passwordEncoder);

        // when & then
        assertThatThrownBy(() -> passwordResetService.verifyCodeAndIssueToken(testEmail, code))
            .isInstanceOf(UnauthorizedException.class);

        verify(resetTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("토큰으로 비밀번호 재설정 성공")
    void resetPasswordWithToken_Success() {
        // given
        String token = "valid-token";
        String newPassword = "newPassword123!";
        String encodedPassword = "encodedNewPassword";

        PasswordResetToken resetToken = mock(PasswordResetToken.class);

        when(resetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(resetToken.getUser()).thenReturn(testUser);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        doNothing().when(resetToken).validateExpiryDate();
        doNothing().when(testUser).changePassword(encodedPassword);

        // when
        passwordResetService.resetPasswordWithToken(token, newPassword);

        // then
        verify(resetToken).validateExpiryDate();
        verify(testUser).changePassword(encodedPassword);
        verify(profileRepository).save(testUser);
        verify(resetTokenRepository).delete(resetToken);
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 비밀번호 재설정 시 예외 발생")
    void resetPasswordWithToken_InvalidToken() {
        // given
        String token = "invalid-token";
        String newPassword = "newPassword123!";

        when(resetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> passwordResetService.resetPasswordWithToken(token, newPassword))
            .isInstanceOf(UnauthorizedException.class);

        verify(profileRepository, never()).save(any());
        verify(resetTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("만료된 토큰으로 비밀번호 재설정 시 예외 발생")
    void resetPasswordWithToken_ExpiredToken() {
        // given
        String token = "expired-token";
        String newPassword = "newPassword123!";

        PasswordResetToken resetToken = mock(PasswordResetToken.class);

        when(resetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        doThrow(new UnauthorizedException("토큰이 만료되었습니다."))
            .when(resetToken).validateExpiryDate();

        // when & then
        assertThatThrownBy(() -> passwordResetService.resetPasswordWithToken(token, newPassword))
            .isInstanceOf(UnauthorizedException.class);

        verify(profileRepository, never()).save(any());
        verify(resetTokenRepository, never()).delete(any());
    }

    private User createUser() {
        return User.create(
            "kim",
            "아마추어",
            "test@email.ac.kr",
            "asdf02~!",
            "mykakao12",
            "FW",
            "강원대학교",
            "컴퓨터공학과",
            "25",
            "즐겜해요~"
        );
    }
}