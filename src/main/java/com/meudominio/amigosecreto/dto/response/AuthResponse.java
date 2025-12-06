package com.meudominio.amigosecreto.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de autenticação com tokens JWT")
public class AuthResponse {

    @Schema(description = "Token de acesso JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Token de renovação", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "Nome de usuário", example = "joaosilva")
    private String username;

    @Schema(description = "Email do usuário", example = "joao.silva@email.com")
    private String email;

    @Schema(description = "Role do usuário", example = "PARTICIPANT")
    private String role;
}
