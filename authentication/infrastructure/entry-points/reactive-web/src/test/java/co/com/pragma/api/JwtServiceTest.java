package co.com.pragma.api;


import co.com.pragma.api.jwt.JwtProperties;
import co.com.pragma.api.jwt.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtProperties props;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        props = Mockito.mock(JwtProperties.class);
        Mockito.when(props.secret()).thenReturn("HsTqW3BTS2qT/U8L8K5l8wV7Q8H3q0EoM7R0JbHcBjc=");
        Mockito.when(props.expirationMs()).thenReturn(1000L * 60 * 60); // 1 hora
        jwtService = new JwtService(props);
    }

    @Test
    void shouldGenerateTokenAndExtractClaims() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, roleId);
        assertNotNull(token);

        Claims claims = jwtService.extractAllClaims(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(roleId.toString(), claims.get("idRol", String.class));
    }

    @Test
    void shouldExtractUserIdAndRoleId() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, roleId);

        UUID extractedUserId = jwtService.extractUserId(token);
        UUID extractedRoleId = jwtService.extractRoleId(token);

        assertEquals(userId, extractedUserId);
        assertEquals(roleId, extractedRoleId);
    }

    @Test
    void shouldReturnTrueForValidToken() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, roleId);

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldReturnFalseForExpiredToken() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        Mockito.when(props.expirationMs()).thenReturn(1L);
        jwtService = new JwtService(props);

        String token = jwtService.generateToken(userId, roleId);

        Thread.sleep(10);

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
        assertThrows(Exception.class, () -> jwtService.extractAllClaims("invalid.token.here"));
    }
}