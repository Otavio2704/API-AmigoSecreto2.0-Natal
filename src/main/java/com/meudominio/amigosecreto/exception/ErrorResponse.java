package com.meudominio.amigosecreto.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estrutura padronizada de resposta de erro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta de erro padronizada")
public class ErrorResponse {

    @Schema(description = "Timestamp do erro", example = "2025-12-06T14:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Código de status HTTP", example = "404")
    private Integer status;

    @Schema(description = "Nome do erro", example = "Not Found")
    private String error;

    @Schema(description = "Mensagem de erro", example = "Grupo não encontrado")
    private String message;

    @Schema(description = "Caminho da requisição", example = "/api/groups/999")
    private String path;

    @Schema(description = "Erros de validação (quando aplicável)")
    private Map<String, String> validationErrors;
}
