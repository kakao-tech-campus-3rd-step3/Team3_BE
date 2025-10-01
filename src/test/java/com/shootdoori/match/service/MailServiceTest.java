package com.shootdoori.match.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailServiceTest {
    @Autowired
    private MailService mailService;

    @Test
    @DisplayName("이메일_실제_발송_테스트")
    void send_email_test() {
        // given
        String toEmail = "shootdoori.official@gmail.com";
        String subject = "스프링 부트 이메일 테스트";
        String code = "SMTP 설정 성공.";

        // when
        mailService.sendEmail(toEmail, subject, code);

        // then
        System.out.println("이메일 발송 완료.");
    }
}
