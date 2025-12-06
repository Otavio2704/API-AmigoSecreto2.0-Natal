package com.meudominio.amigosecreto.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta com dados do grupo")
public class GroupResponse {

    @Schema(description = "ID do grupo", example = "1")
    private Long id;

    @Schema(description = "Nome do grupo", example = "Amigo Secreto 2025")
    private String name;

    @Schema(description = "Descrição do grupo", example = "Confraternização de fim de ano da empresa")
    private String description;

    @Schema(description = "Username do administrador", example = "joaosilva")
    private String adminUsername;

    @Schema(description = "Data prevista para o sorteio", example = "2025-12-20")
    private LocalDate drawDate;

    @Schema(description = "Número de membros no grupo", example = "8")
    private Integer memberCount;

    @Schema(description = "Lista com usernames dos membros")
    private List<String> members;

    @Schema(description = "Data de criação do grupo", example = "2025-12-01T14:30:00")
    private LocalDateTime createdAt;
}
