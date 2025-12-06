package com.meudominio.amigosecreto.controller;

import com.meudominio.amigosecreto.dto.request.CreateGroupRequest;
import com.meudominio.amigosecreto.dto.response.GroupResponse;
import com.meudominio.amigosecreto.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Grupos", description = "Gerenciamento de grupos de amigo secreto")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @Operation(summary = "Criar novo grupo", description = "Cria um grupo de amigo secreto (usuário se torna administrador)")
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        GroupResponse response = groupService.createGroup(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar grupos do usuário", description = "Retorna todos os grupos que o usuário participa")
    public ResponseEntity<List<GroupResponse>> getUserGroups(Authentication authentication) {
        String username = authentication.getName();
        List<GroupResponse> groups = groupService.getUserGroups(username);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter detalhes do grupo", description = "Retorna informações detalhadas de um grupo específico")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        GroupResponse response = groupService.getGroupById(id, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Adicionar membro ao grupo", description = "Adiciona um participante ao grupo (apenas administrador)")
    public ResponseEntity<Void> addMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            Authentication authentication) {
        String username = authentication.getName();
        groupService.addMember(id, userId, username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remover membro do grupo", description = "Remove um participante do grupo (apenas administrador)")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication) {
        String username = authentication.getName();
        groupService.removeMember(id, userId, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar grupo", description = "Remove o grupo permanentemente (apenas administrador)")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        groupService.deleteGroup(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Bloquear usuário", description = "Impede que um usuário tire outro no sorteio")
    public ResponseEntity<Void> blockUser(
            @PathVariable Long id,
            @RequestParam Long blockedUserId,
            Authentication authentication) {
        String username = authentication.getName();
        groupService.blockUser(id, username, blockedUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
