package com.meudominio.amigosecreto.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição de criação de grupo")
public class CreateGroupRequest {

    @NotBlank(message = "Nome do grupo é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Schema(description = "Nome do grupo", example = "Amigo Secreto 2025")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Schema(description = "Descrição do grupo", example = "Confraternização de fim de ano da empresa")
    private String description;

    @FutureOrPresent(message = "Data do sorteio deve ser hoje ou no futuro")
    @Schema(description = "Data prevista para o sorteio", example = "2025-12-20")
    private LocalDate drawDate;
}
