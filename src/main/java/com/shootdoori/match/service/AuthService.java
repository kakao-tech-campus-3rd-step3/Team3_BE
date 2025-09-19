package com.shootdoori.match.service;

import com.shootdoori.match.dto.AuthToken;
import com.shootdoori.match.dto.LoginRequest;
import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtUtil jwtUtil, ProfileService profileService, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.profileService = profileService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthToken register(ProfileCreateRequest request) {
        profileService.createProfile(request);

        User savedUser = profileService.findByEmail(request.email())
            .orElseThrow(() -> new RuntimeException("회원가입에 실패하였습니다."));

        return new AuthToken(jwtUtil.generateAccessToken(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthToken login(LoginRequest request) {
        User user = profileService.findByEmail(request.email())
            .orElseThrow(() -> new RuntimeException("잘못된 이메일 또는 비밀번호입니다."));

        user.samePassword(request.password(), passwordEncoder);
        String accessToken = jwtUtil.generateAccessToken(user);

        return new AuthToken(accessToken);
    }
}
