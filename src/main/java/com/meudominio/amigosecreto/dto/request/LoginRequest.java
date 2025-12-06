package com.meudominio.amigosecreto.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição de login")
public class LoginRequest {

    @NotBlank(message = "Username é obrigatório")
    @Schema(description = "Nome de usuário", example = "joaosilva")
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    @Schema(description = "Senha do usuário", example = "senha123")
    private String password;
}
