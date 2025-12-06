package com.meudominio.amigosecreto.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição de envio de mensagem")
public class SendMessageRequest {

    @NotNull(message = "ID do grupo é obrigatório")
    @Schema(description = "ID do grupo onde a mensagem será enviada", example = "1")
    private Long groupId;

    @NotBlank(message = "Conteúdo da mensagem é obrigatório")
    @Size(min = 1, max = 1000, message = "Mensagem deve ter entre 1 e 1000 caracteres")
    @Schema(description = "Conteúdo da mensagem", example = "Olá pessoal! Estou ansioso para o sorteio!")
    private String content;

    @Schema(description = "Se a mensagem é anônima (padrão: true)", example = "true")
    @Builder.Default
    private Boolean isAnonymous = true;
}
