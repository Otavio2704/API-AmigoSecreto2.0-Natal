package com.meudominio.amigosecreto.service;

import com.meudominio.amigosecreto.dto.request.LoginRequest;
import com.meudominio.amigosecreto.dto.request.RegisterRequest;
import com.meudominio.amigosecreto.dto.response.AuthResponse;
import com.meudominio.amigosecreto.exception.BusinessException;
import com.meudominio.amigosecreto.model.User;
import com.meudominio.amigosecreto.model.enums.Role;
import com.meudominio.amigosecreto.repository.UserRepository;
import com.meudominio.amigosecreto.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Testes Unitários")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("joaosilva")
                .email("joao@email.com")
                .password("senha_encoded")
                .role(Role.PARTICIPANT)
                .build();

        registerRequest = RegisterRequest.builder()
                .username("joaosilva")
                .email("joao@email.com")
                .password("senha123")
                .build();

        loginRequest = LoginRequest.builder()
                .username("joaosilva")
                .password("senha123")
                .build();
    }

    // ========================
    // REGISTER
    // ========================

    @Test
    @DisplayName("register - deve registrar usuário com sucesso")
    void register_deveRegistrarUsuarioComSucesso() {
        when(userRepository.existsByUsername("joaosilva")).thenReturn(false);
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senha_encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken("joaosilva")).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken("joaosilva")).thenReturn("refresh_token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("joaosilva");
        assertThat(response.getEmail()).isEqualTo("joao@email.com");
        assertThat(response.getToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(response.getRole()).isEqualTo("PARTICIPANT");

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    @DisplayName("register - deve lançar exceção quando username já existe")
    void register_deveLancarExcecaoQuandoUsernameJaExiste() {
        when(userRepository.existsByUsername("joaosilva")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Username já está em uso");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - deve lançar exceção quando email já existe")
    void register_deveLancarExcecaoQuandoEmailJaExiste() {
        when(userRepository.existsByUsername("joaosilva")).thenReturn(false);
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email já está em uso");

        verify(userRepository, never()).save(any());
    }

    // ========================
    // LOGIN
    // ========================

    @Test
    @DisplayName("login - deve autenticar usuário com sucesso")
    void login_deveAutenticarUsuarioComSucesso() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("joaosilva")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("joaosilva")).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken("joaosilva")).thenReturn("refresh_token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("joaosilva");
        assertThat(response.getToken()).isEqualTo("access_token");
    }

    @Test
    @DisplayName("login - deve lançar exceção quando credenciais são inválidas")
    void login_deveLancarExcecaoQuandoCredenciaisInvalidas() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ========================
    // REFRESH TOKEN
    // ========================

    @Test
    @DisplayName("refreshToken - deve renovar token com sucesso")
    void refreshToken_deveRenovarTokenComSucesso() {
        when(jwtTokenProvider.validateToken("valid_refresh")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid_refresh")).thenReturn("joaosilva");
        when(userRepository.findByUsername("joaosilva")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("joaosilva")).thenReturn("new_access_token");
        when(jwtTokenProvider.generateRefreshToken("joaosilva")).thenReturn("new_refresh_token");

        AuthResponse response = authService.refreshToken("valid_refresh");

        assertThat(response.getToken()).isEqualTo("new_access_token");
        assertThat(response.getRefreshToken()).isEqualTo("new_refresh_token");
    }

    @Test
    @DisplayName("refreshToken - deve remover prefixo 'Bearer ' automaticamente")
    void refreshToken_deveRemoverPrefixoBearer() {
        when(jwtTokenProvider.validateToken("valid_refresh")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid_refresh")).thenReturn("joaosilva");
        when(userRepository.findByUsername("joaosilva")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("new_token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("new_refresh");

        AuthResponse response = authService.refreshToken("Bearer valid_refresh");

        assertThat(response).isNotNull();
        verify(jwtTokenProvider).validateToken("valid_refresh"); // sem "Bearer "
    }

    @Test
    @DisplayName("refreshToken - deve lançar exceção quando token é inválido")
    void refreshToken_deveLancarExcecaoQuandoTokenInvalido() {
        when(jwtTokenProvider.validateToken("invalid_token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("invalid_token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token inválido ou expirado");
    }
}
