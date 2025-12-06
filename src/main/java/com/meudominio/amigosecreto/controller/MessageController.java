package com.meudominio.amigosecreto.controller;

import com.meudominio.amigosecreto.dto.request.SendMessageRequest;
import com.meudominio.amigosecreto.dto.response.MessageResponse;
import com.meudominio.amigosecreto.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Mensagens", description = "Sistema de mensagens anônimas entre participantes")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @Operation(summary = "Enviar mensagem", description = "Envia uma mensagem (anônima ou identificada) para o grupo")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        MessageResponse response = messageService.sendMessage(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Listar mensagens do grupo", description = "Retorna todas as mensagens de um grupo específico")
    public ResponseEntity<List<MessageResponse>> getGroupMessages(
            @PathVariable Long groupId,
            Authentication authentication) {
        String username = authentication.getName();
        List<MessageResponse> messages = messageService.getGroupMessages(groupId, username);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter mensagem por ID", description = "Retorna detalhes de uma mensagem específica")
    public ResponseEntity<MessageResponse> getMessageById(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        MessageResponse response = messageService.getMessageById(id, username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar mensagem", description = "Remove uma mensagem (apenas o remetente ou admin do grupo)")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        messageService.deleteMessage(id, username);
        return ResponseEntity.noContent().build();
    }
}
