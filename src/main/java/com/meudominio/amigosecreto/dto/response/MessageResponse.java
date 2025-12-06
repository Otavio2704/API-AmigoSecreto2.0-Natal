package com.meudominio.amigosecreto.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta com dados da mensagem")
public class MessageResponse {

    @Schema(description = "ID da mensagem", example = "1")
    private Long id;

    @Schema(description = "ID do grupo", example = "1")
    private Long groupId;

    @Schema(description = "Nome do grupo", example = "Amigo Secreto 2025")
    private String groupName;

    @Schema(description = "Username do remetente (ou 'Anônimo' se isAnonymous=true)", example = "joaosilva")
    private String senderUsername;

    @Schema(description = "Conteúdo da mensagem", example = "Olá pessoal! Estou ansioso para o sorteio!")
    private String content;

    @Schema(description = "Se a mensagem é anônima", example = "true")
    private Boolean isAnonymous;

    @Schema(description = "Data e hora do envio", example = "2025-12-06T14:30:00")
    private LocalDateTime timestamp;
}
