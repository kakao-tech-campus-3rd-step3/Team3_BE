package com.shootdoori.match.config;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderService {
    private static PasswordEncoder staticPasswordEncoder;
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public PasswordEncoderService() {
        staticPasswordEncoder = passwordEncoder;
    }

    public PasswordEncoder getEncoder() {
        return passwordEncoder;
    }

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return staticPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}
