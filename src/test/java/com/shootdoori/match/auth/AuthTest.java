package com.shootdoori.match.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shootdoori.match.dto.AuthToken;
import com.shootdoori.match.dto.LoginRequest;
import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("인증 통합 테스트")
class AuthTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private AuthService authService;
    @Autowired private ObjectMapper objectMapper;

    @Nested
    @DisplayName("회원가입 (/api/auth/register)")
    class RegisterTests {

        @Test
        @DisplayName("성공: 새로운 사용자가 정상적으로 회원가입된다")
        void registerSuccess() throws Exception {
            ProfileCreateRequest request = AuthFixtures.createProfileRequest();

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"));
        }

        @Test
        @DisplayName("실패: 이미 존재하는 이메일로 회원가입 시 409 Conflict 에러가 발생한다")
        void registerFailByDuplicateEmail() throws Exception {
            authService.register(
                AuthFixtures.createProfileRequest(),
                new MockHttpServletRequest()
            );
            ProfileCreateRequest duplicateRequest = AuthFixtures.createProfileRequest();

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("로그인 (/api/auth/login)")
    class LoginTests {

        @BeforeEach
        void setup() {
            authService.register(
                AuthFixtures.createProfileRequest(),
                new MockHttpServletRequest()
            );
        }

        @Test
        @DisplayName("성공: 기존 사용자가 올바른 정보로 로그인한다")
        void loginSuccess() throws Exception {
            LoginRequest request = AuthFixtures.createLoginRequest();

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일로 로그인 시 401 Unauthorized 에러가 발생한다")
        void loginFailByNonExistentEmail() throws Exception {
            LoginRequest request = new LoginRequest(
                "nonexistent@test.com",
                AuthFixtures.USER_PASSWORD
            );

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패: 비밀번호가 틀렸을 경우 401 Unauthorized 에러가 발생한다")
        void loginFailByWrongPassword() throws Exception {
            LoginRequest request = new LoginRequest(
                AuthFixtures.USER_EMAIL,
                "wrongpassword"
            );

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 (/api/auth/refresh)")
    class TokenRefreshTests {

        @Test
        @DisplayName("성공: 유효한 리프레시 토큰으로 재발급 후, 기존 토큰 사용 시 실패한다 (Rotation 검증)")
        void tokenRefreshAndRotationSuccess() throws Exception {
            AuthToken initialTokens = authService.register(
                AuthFixtures.createProfileRequest(),
                new MockHttpServletRequest()
            );

            Cookie refreshTokenCookie = new Cookie(
                "refreshToken",
                initialTokens.refreshToken()
            );

            mockMvc.perform(post("/api/auth/refresh")
                    .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"));

            mockMvc.perform(post("/api/auth/refresh")
                    .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized());
        }
    }

    private static class AuthFixtures {
        public static final String USER_EMAIL = "test@test.com";
        public static final String UNIVERSITY_EMAIL = "university@test.ac.kr";
        public static final String USER_PASSWORD = "password25~!";

        public static ProfileCreateRequest createProfileRequest() {
            return new ProfileCreateRequest(
                "tester", "아마추어", USER_EMAIL, UNIVERSITY_EMAIL, USER_PASSWORD,
                "010-1234-5678", "공격수", "강원대학교", "컴퓨터공학과", "25", "안녕하세요"
            );
        }

        public static LoginRequest createLoginRequest() {
            return new LoginRequest(USER_EMAIL, USER_PASSWORD);
        }
    }
}