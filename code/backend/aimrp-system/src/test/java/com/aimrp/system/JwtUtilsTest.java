package com.aimrp.system;

import com.aimrp.system.infrastructure.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtils TDD Tests
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret",
            "test-secret-key-for-jwt-token-generation-must-be-long-enough-32chars");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 86400000L);
    }

    // ===== Generate Token Tests =====

    @Test
    void generateToken_shouldReturnValidToken() {
        // Given
        Long userId = 123L;
        String username = "testuser";

        // When
        String token = jwtUtils.generateToken(userId, username);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void generateToken_differentUsersShouldHaveDifferentTokens() {
        // Given
        String username1 = "user1";
        String username2 = "user2";

        // When
        String token1 = jwtUtils.generateToken(1L, username1);
        String token2 = jwtUtils.generateToken(2L, username2);

        // Then
        assertNotEquals(token1, token2);
    }

    // ===== Parse Token Tests =====

    @Test
    void parseToken_shouldReturnClaims() {
        // Given
        String token = jwtUtils.generateToken(123L, "testuser");

        // When
        var claims = jwtUtils.parseToken(token);

        // Then
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
    }

    // ===== Validate Token Tests =====

    @Test
    void validateToken_validToken_shouldReturnTrue() {
        // Given
        String token = jwtUtils.generateToken(123L, "testuser");

        // When
        boolean valid = jwtUtils.validateToken(token);

        // Then
        assertTrue(valid);
    }

    @Test
    void validateToken_invalidToken_shouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        boolean valid = jwtUtils.validateToken(invalidToken);

        // Then
        assertFalse(valid);
    }

    @Test
    void validateToken_nullToken_shouldReturnFalse() {
        // Given
        String nullToken = null;

        // When
        boolean valid = jwtUtils.validateToken(nullToken);

        // Then
        assertFalse(valid);
    }

    // ===== Get User ID Tests =====

    @Test
    void getUserId_shouldReturnCorrectUserId() {
        // Given
        Long userId = 123L;
        String token = jwtUtils.generateToken(userId, "testuser");

        // When
        Long extractedUserId = jwtUtils.getUserId(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    // ===== Get Username Tests =====

    @Test
    void getUsername_shouldReturnCorrectUsername() {
        // Given
        String username = "testuser";
        String token = jwtUtils.generateToken(123L, username);

        // When
        String extractedUsername = jwtUtils.getUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void getUsername_fromValidToken_shouldReturnSubject() {
        // Given
        String token = jwtUtils.generateToken(1L, "admin");

        // When
        String subject = jwtUtils.getUsername(token);

        // Then
        assertEquals("admin", subject);
    }
}
