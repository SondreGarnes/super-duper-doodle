package com.superduperdoodle.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // A valid Base64-encoded 256-bit secret for HMAC-SHA256
    private static final String TEST_SECRET =
        "dGVzdFNlY3JldEtleUZvclVuaXRUZXN0aW5nUHVycG9zZXM="; // 32 bytes decoded
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION_MS);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtService.generateToken("alice");

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsername_returnsCorrectSubject() {
        String token = jwtService.generateToken("alice");

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("alice");
    }

    @Test
    void isTokenValid_withMatchingUser_returnsTrue() {
        String token = jwtService.generateToken("alice");
        UserDetails userDetails = User.withUsername("alice")
            .password("irrelevant")
            .authorities(List.of())
            .build();

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_withDifferentUsername_returnsFalse() {
        String token = jwtService.generateToken("alice");
        UserDetails userDetails = User.withUsername("bob")
            .password("irrelevant")
            .authorities(List.of())
            .build();

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_withMalformedToken_returnsFalse() {
        UserDetails userDetails = User.withUsername("alice")
            .password("irrelevant")
            .authorities(List.of())
            .build();

        boolean valid = jwtService.isTokenValid("not.a.valid.token", userDetails);

        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_withExpiredToken_returnsFalse() {
        // Override expiration to -1ms so token is already expired
        ReflectionTestUtils.setField(jwtService, "expiration", -1L);
        String expiredToken = jwtService.generateToken("alice");

        UserDetails userDetails = User.withUsername("alice")
            .password("irrelevant")
            .authorities(List.of())
            .build();

        boolean valid = jwtService.isTokenValid(expiredToken, userDetails);

        assertThat(valid).isFalse();
    }
}
