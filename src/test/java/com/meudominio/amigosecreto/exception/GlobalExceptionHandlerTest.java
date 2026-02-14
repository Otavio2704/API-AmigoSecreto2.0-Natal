package com.meudominio.amigosecreto.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Testes Unitários")
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private WebRequest request;

    @BeforeEach
    void setUp() {
        when(request.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("handleResourceNotFoundException - deve retornar 404")
    void handleResourceNotFoundException_deveRetornar404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Grupo não encontrado");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Grupo não encontrado");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("handleUnauthorizedException - deve retornar 403")
    void handleUnauthorizedException_deveRetornar403() {
        UnauthorizedException ex = new UnauthorizedException("Acesso negado");

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("handleBusinessException - deve retornar 400")
    void handleBusinessException_deveRetornar400() {
        BusinessException ex = new BusinessException("Regra de negócio violada");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Regra de negócio violada");
    }

    @Test
    @DisplayName("handleValidationExceptions - deve retornar 400 com mapa de erros")
    void handleValidationExceptions_deveRetornarErrosDeValidacao() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("object", "username", "Username é obrigatório");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getValidationErrors()).containsEntry("username", "Username é obrigatório");
    }

    @Test
    @DisplayName("handleBadCredentialsException - deve retornar 401")
    void handleBadCredentialsException_deveRetornar401() {
        BadCredentialsException ex = new BadCredentialsException("Credenciais inválidas");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentialsException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Username ou senha inválidos");
    }

    @Test
    @DisplayName("handleAccessDeniedException - deve retornar 403")
    void handleAccessDeniedException_deveRetornar403() {
        AccessDeniedException ex = new AccessDeniedException("Acesso negado");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getMessage()).contains("permissão");
    }

    @Test
    @DisplayName("handleExpiredJwtException - deve retornar 401")
    void handleExpiredJwtException_deveRetornar401() {
        ExpiredJwtException ex = mock(ExpiredJwtException.class);
        when(ex.getMessage()).thenReturn("Token expirado");

        ResponseEntity<ErrorResponse> response = handler.handleExpiredJwtException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).contains("expirado");
    }

    @Test
    @DisplayName("handleMalformedJwtException - deve retornar 401")
    void handleMalformedJwtException_deveRetornar401() {
        MalformedJwtException ex = mock(MalformedJwtException.class);
        when(ex.getMessage()).thenReturn("Token inválido");

        ResponseEntity<ErrorResponse> response = handler.handleMalformedJwtException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Token JWT inválido");
    }

    @Test
    @DisplayName("handleGlobalException - deve retornar 500 para erros genéricos")
    void handleGlobalException_deveRetornar500() {
        Exception ex = new RuntimeException("Erro inesperado");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("erro interno");
    }

    @Test
    @DisplayName("ErrorResponse - deve conter timestamp ao ser criado")
    void errorResponse_deveConterTimestamp() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Teste");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, request);

        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("ResourceNotFoundException - construtor com nome do recurso")
    void resourceNotFoundException_construtorComNomeDoRecurso() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Grupo", "id", 99L);

        assertThat(ex.getMessage()).contains("Grupo");
        assertThat(ex.getMessage()).contains("id");
        assertThat(ex.getMessage()).contains("99");
    }
}
