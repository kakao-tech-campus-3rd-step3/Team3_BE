package com.shootdoori.match.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shootdoori.match.dto.*;
import com.shootdoori.match.entity.common.DeviceType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.user.UserStatus;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.RefreshTokenRepository;
import com.shootdoori.match.service.AuthService;
import com.shootdoori.match.util.JwtUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("인증 통합 테스트")
class AuthTest {
    @Autowired private EntityManager entityManager;
    @Autowired private MockMvc mockMvc;
    @Autowired private AuthService authService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private JwtUtil jwtUtil;

    private final ClientInfo DEFAULT_CLIENT_INFO = new ClientInfo("JUnit-Test-Agent", DeviceType.ANDROID);

    private String stripBearer(String token) {
        return token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    @Nested
    @DisplayName("회원가입 (/api/auth/register)")
    @Transactional
    class RegisterTests {

        @BeforeEach
        void setup() {
            refreshTokenRepository.deleteAll();
            profileRepository.deleteAll();
        }

        @Test
        @DisplayName("성공: 새로운 사용자가 정상적으로 회원가입된다")
        void registerSuccess() throws Exception {
            ProfileCreateRequest request = AuthFixtures.createProfileRequest();

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists());
        }

        @Test
        @DisplayName("실패: 이미 존재하는 이메일로 회원가입 시 409 Conflict 에러가 발생한다")
        void registerFailByDuplicateEmail() throws Exception {
            authService.register(
                AuthFixtures.createProfileRequest(),
                DEFAULT_CLIENT_INFO
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
    @Transactional
    class LoginTests {

        @BeforeEach
        void setup() {
            refreshTokenRepository.deleteAll();
            profileRepository.deleteAll();
            authService.register(
                AuthFixtures.createProfileRequest(),
                DEFAULT_CLIENT_INFO
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
                    .andExpect(jsonPath("$.refreshToken").exists());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일로 로그인 시 400 Bad Request 에러가 발생한다")
        void loginFailByNonExistentEmail() throws Exception {
            LoginRequest request = new LoginRequest(
                "nonexistent@test.ac.kr",
                AuthFixtures.USER_PASSWORD
            );

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 비밀번호가 틀렸을 경우 400 Bad Request 에러가 발생한다")
        void loginFailByWrongPassword() throws Exception {
            LoginRequest request = new LoginRequest(
                AuthFixtures.USER_EMAIL,
                "wrongpassword"
            );

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 이메일 형식이 잘못된 경우 400 Bad Request 에러가 발생한다")
        void loginFailByInvalidEmailFormat() throws Exception {
            // 이메일 형식이 잘못된 경우 (학교 이메일 아님)
            LoginRequest request = new LoginRequest(
                "invalid_email@gmail.com",
                AuthFixtures.USER_PASSWORD
            );

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("로그아웃 (/api/auth/logout, /logout-all)")
    class LogoutTests {

        private AuthToken initialTokens;

        @BeforeEach
        void setup() {
            // 깨끗한 상태로 시작
            refreshTokenRepository.deleteAll();
            profileRepository.deleteAll();

            authService.register(
                AuthFixtures.createProfileRequest(),
                DEFAULT_CLIENT_INFO
            );
            initialTokens = authService.login(
                AuthFixtures.createLoginRequest(),
                DEFAULT_CLIENT_INFO
            );
        }

        @Test
        @DisplayName("성공: 현재 기기에서 로그아웃한다")
        void logoutSuccess() throws Exception {
            String accessToken = initialTokens.accessToken();
            String refreshToken = initialTokens.refreshToken();

            String tokenId = jwtUtil.getClaims(refreshToken).getId();

            mockMvc.perform(post("/api/auth/logout")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new TokenRefreshRequest(refreshToken))))
                    .andExpect(status().isOk());

            assertThat(refreshTokenRepository.findById(tokenId)).isEmpty();
        }

        @Test
        @DisplayName("성공: 모든 기기에서 로그아웃한다")
        void logoutAllSuccess() throws Exception {
            AuthToken otherDeviceLoginTokens = authService.login(
                AuthFixtures.createLoginRequest(),
                DEFAULT_CLIENT_INFO
            );

            String accessToken = otherDeviceLoginTokens.accessToken();
            Long userId = Long.parseLong(jwtUtil.getUserId(stripBearer(accessToken)));

            String refreshToken = otherDeviceLoginTokens.refreshToken();

            mockMvc.perform(post("/api/auth/logout-all")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new TokenRefreshRequest(refreshToken))))
                    .andExpect(status().isOk());

            assertThat(refreshTokenRepository.countByUserId(userId)).isZero();
        }

        @Test
        @DisplayName("실패: 인증 없이 모든 기기 로그아웃 요청 시 401 Unauthorized 에러가 발생한다")
        void logoutAllFailWithoutAuth() throws Exception {

            mockMvc.perform(post("/api/auth/logout-all"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 (/api/auth/refresh)")
    class TokenRefreshTests {

        @BeforeEach
        void setup() {
            refreshTokenRepository.deleteAll();
            profileRepository.deleteAll();
        }

        @Test
        @DisplayName("성공: 유효한 리프레시 토큰으로 재발급 후, 기존 토큰 사용 시 실패한다 (Rotation 검증)")
        void tokenRefreshAndRotationSuccess() throws Exception {
            AuthToken initialTokens = authService.register(
                AuthFixtures.createProfileRequest(),
                DEFAULT_CLIENT_INFO
            );

            String initialRefresh = initialTokens.refreshToken();

            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new TokenRefreshRequest(initialRefresh))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists());

            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new TokenRefreshRequest(initialRefresh))))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("회원탈퇴 (/api/profiles/me)")
    @Transactional
    class DeleteAccountTests {

        private String accessToken;
        private Long userId;

        @BeforeEach
        void setup() {
            refreshTokenRepository.deleteAll();
            profileRepository.deleteAll();

            authService.register(
                AuthFixtures.createProfileRequest(),
                DEFAULT_CLIENT_INFO
            );
            AuthToken tokens = authService.login(
                AuthFixtures.createLoginRequest(),
                DEFAULT_CLIENT_INFO
            );
            accessToken = tokens.accessToken();
            userId = Long.parseLong(jwtUtil.getUserId(stripBearer(accessToken)));
        }

        @Test
        @DisplayName("성공: 로그인된 사용자가 정상적으로 회원 탈퇴를 요청하고, 상태가 DELETED로 변경된다")
        void deleteAccountSuccess() throws Exception {
            mockMvc.perform(delete("/api/profiles/me")
                    .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent());

            entityManager.flush();
            entityManager.clear();

            User userAfterDelete = profileRepository.findByIdIncludingDeleted(userId)
                .orElseThrow();
            assertThat(userAfterDelete.getStatus()).isEqualTo(UserStatus.DELETED);
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 회원탈퇴 요청 시 401 Unauthorized 에러가 발생한다")
        void deleteAccountFailWithoutAuth() throws Exception {
            mockMvc.perform(delete("/api/profiles/me"))
                .andExpect(status().isUnauthorized());
        }
    }

    private static class AuthFixtures {
        public static final String USER_EMAIL = "university@test.ac.kr";
        public static final String USER_PASSWORD = "password25~!";

        public static ProfileCreateRequest createProfileRequest() {
            return new ProfileCreateRequest(
                "tester", "아마추어", USER_EMAIL, USER_PASSWORD,
                "imkim250", "FW", "강원대학교", "컴퓨터공학과", "25", "안녕하세요"
            );
        }

        public static LoginRequest createLoginRequest() {
            return new LoginRequest(USER_EMAIL, USER_PASSWORD);
        }
    }
}
