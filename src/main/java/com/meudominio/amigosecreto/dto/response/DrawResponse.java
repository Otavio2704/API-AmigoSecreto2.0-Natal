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
@Schema(description = "Resposta com resultado do sorteio")
public class DrawResponse {

    @Schema(description = "ID do sorteio", example = "1")
    private Long id;

    @Schema(description = "ID do grupo", example = "1")
    private Long groupId;

    @Schema(description = "Nome do grupo", example = "Amigo Secreto 2025")
    private String groupName;

    @Schema(description = "Username de quem tirou", example = "joaosilva")
    private String giverUsername;

    @Schema(description = "Username de quem foi tirado (amigo secreto)", example = "mariasantos")
    private String receiverUsername;
}
