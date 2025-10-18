package com.shootdoori.match.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    @DisplayName("이메일_메시지_구성_및_발송_호출_검증")
    void send_email_mocked_sender() {
        // given
        ReflectionTestUtils.setField(mailService, "fromAddress", "noreply@test.local");
        String toEmail = "shootdoori.official@gmail.com";
        String subject = "스프링 부트 이메일 테스트";
        String code = "SMTP 설정 성공.";

        // when
        mailService.sendEmail(toEmail, subject, code);

        // then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getFrom()).isEqualTo("noreply@test.local");
        assertThat(sent.getTo()).containsExactly(toEmail);
        assertThat(sent.getSubject()).isEqualTo(subject);
        assertThat(sent.getText()).isEqualTo(code);
    }
}
