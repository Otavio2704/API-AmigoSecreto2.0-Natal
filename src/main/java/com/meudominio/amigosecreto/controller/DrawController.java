package com.meudominio.amigosecreto.controller;

import com.meudominio.amigosecreto.dto.response.DrawResponse;
import com.meudominio.amigosecreto.service.DrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Sorteios", description = "Execução e consulta de sorteios de amigo secreto")
public class DrawController {

    private final DrawService drawService;

    @PostMapping("/draw")
    @Operation(summary = "Executar sorteio", description = "Realiza o sorteio do amigo secreto para o grupo (apenas administrador)")
    public ResponseEntity<List<DrawResponse>> executeDraw(
            @PathVariable Long groupId,
            Authentication authentication) {
        String username = authentication.getName();
        List<DrawResponse> results = drawService.executeDraw(groupId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    @GetMapping("/my-draw")
    @Operation(summary = "Ver meu amigo secreto", description = "Retorna quem o usuário tirou no sorteio")
    public ResponseEntity<DrawResponse> getMyDraw(
            @PathVariable Long groupId,
            Authentication authentication) {
        String username = authentication.getName();
        DrawResponse response = drawService.getMyDraw(groupId, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/draw/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ver todos os sorteios", description = "Retorna todos os resultados do sorteio (apenas administrador do grupo)")
    public ResponseEntity<List<DrawResponse>> getAllDraws(
            @PathVariable Long groupId,
            Authentication authentication) {
        String username = authentication.getName();
        List<DrawResponse> results = drawService.getAllDraws(groupId, username);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/draw")
    @Operation(summary = "Resetar sorteio", description = "Remove o sorteio atual para refazê-lo (apenas administrador)")
    public ResponseEntity<Void> resetDraw(
            @PathVariable Long groupId,
            Authentication authentication) {
        String username = authentication.getName();
        drawService.resetDraw(groupId, username);
        return ResponseEntity.noContent().build();
    }
}
