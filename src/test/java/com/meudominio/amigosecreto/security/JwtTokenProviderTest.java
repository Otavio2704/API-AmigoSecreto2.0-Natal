package com.meudominio.amigosecreto.security;

import com.meudominio.amigosecreto.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider - Testes Unitários")
public class JwtTokenProviderTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "test-secret-key-for-testing-only-must-be-256-bits-long-xxxxxxxxxx";
    private static final long EXPIRATION = 86400000L;       // 24h
    private static final long REFRESH_EXP = 604800000L;    // 7d

    @BeforeEach
    void setUp() {
        when(jwtConfig.getSecretKey()).thenReturn(SECRET);
        when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);
        when(jwtConfig.getRefreshExpiration()).thenReturn(REFRESH_EXP);
    }

    // ========================
    // GENERATE TOKEN
    // ========================

    @Test
    @DisplayName("generateToken - deve gerar token não nulo")
    void generateToken_deveGerarTokenNaoNulo() {
        String token = jwtTokenProvider.generateToken("joaosilva");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateToken - tokens para usuários diferentes devem ser distintos")
    void generateToken_tokensDevemSerDistintos() {
        String token1 = jwtTokenProvider.generateToken("joaosilva");
        String token2 = jwtTokenProvider.generateToken("mariasantos");
        assertThat(token1).isNotEqualTo(token2);
    }

    // ========================
    // GET USERNAME FROM TOKEN
    // ========================

    @Test
    @DisplayName("getUsernameFromToken - deve extrair username corretamente")
    void getUsernameFromToken_deveExtrairUsername() {
        String token = jwtTokenProvider.generateToken("joaosilva");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertThat(username).isEqualTo("joaosilva");
    }

    // ========================
    // VALIDATE TOKEN
    // ========================

    @Test
    @DisplayName("validateToken - deve retornar true para token válido")
    void validateToken_deveRetornarTrueParaTokenValido() {
        String token = jwtTokenProvider.generateToken("joaosilva");
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken - deve retornar false para token malformado")
    void validateToken_deveRetornarFalseParaTokenMalformado() {
        assertThat(jwtTokenProvider.validateToken("token.invalido.aqui")).isFalse();
    }

    @Test
    @DisplayName("validateToken - deve retornar false para token vazio")
    void validateToken_deveRetornarFalseParaTokenVazio() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("validateToken - deve retornar false para token expirado")
    void validateToken_deveRetornarFalseParaTokenExpirado() {
        when(jwtConfig.getExpiration()).thenReturn(-1000L); // já expirado

        String expiredToken = jwtTokenProvider.generateToken("joaosilva");
        assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
    }

    // ========================
    // REFRESH TOKEN
    // ========================

    @Test
    @DisplayName("generateRefreshToken - deve gerar refresh token válido")
    void generateRefreshToken_deveGerarRefreshTokenValido() {
        String refreshToken = jwtTokenProvider.generateRefreshToken("joaosilva");

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(refreshToken)).isEqualTo("joaosilva");
    }

    @Test
    @DisplayName("generateRefreshToken - access e refresh token devem ser diferentes")
    void generateRefreshToken_deveSerDiferenteDoAccessToken() {
        String access = jwtTokenProvider.generateToken("joaosilva");
        String refresh = jwtTokenProvider.generateRefreshToken("joaosilva");
        assertThat(access).isNotEqualTo(refresh);
    }
}
