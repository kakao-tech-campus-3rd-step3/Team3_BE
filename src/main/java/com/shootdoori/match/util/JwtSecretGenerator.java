package com.shootdoori.match.util;

import java.util.Base64;
import java.security.SecureRandom;

public class JwtSecretGenerator {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[48]; // 48 bytes = 384 bits
        random.nextBytes(bytes);
        String secret = Base64.getEncoder().encodeToString(bytes);
        System.out.println(secret);
    }
}