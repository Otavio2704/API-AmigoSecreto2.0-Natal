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
@Schema(description = "Resposta com dados do usuário")
public class UserResponse {

    @Schema(description = "ID do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome de usuário", example = "joaosilva")
    private String username;

    @Schema(description = "Email do usuário", example = "joao.silva@email.com")
    private String email;

    @Schema(description = "Role do usuário (ADMIN ou PARTICIPANT)", example = "PARTICIPANT")
    private String role;
}
