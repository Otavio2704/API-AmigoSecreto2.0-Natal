package com.meudominio.amigosecreto.service;

import com.meudominio.amigosecreto.dto.request.SendMessageRequest;
import com.meudominio.amigosecreto.dto.response.MessageResponse;
import com.meudominio.amigosecreto.exception.ResourceNotFoundException;
import com.meudominio.amigosecreto.exception.UnauthorizedException;
import com.meudominio.amigosecreto.model.Group;
import com.meudominio.amigosecreto.model.Message;
import com.meudominio.amigosecreto.model.User;
import com.meudominio.amigosecreto.repository.GroupMemberRepository;
import com.meudominio.amigosecreto.repository.GroupRepository;
import com.meudominio.amigosecreto.repository.MessageRepository;
import com.meudominio.amigosecreto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, String username) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar se é membro
        if (!groupMemberRepository.existsByGroupAndUser(group, sender)) {
            throw new UnauthorizedException("Você não é membro deste grupo");
        }

        Message message = Message.builder()
                .group(group)
                .sender(sender)
                .content(request.getContent())
                .isAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : true)
                .timestamp(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        return mapToResponse(message);
    }

    public List<MessageResponse> getGroupMessages(Long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar se é membro
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new UnauthorizedException("Você não é membro deste grupo");
        }

        return messageRepository.findByGroupOrderByTimestampDesc(group).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MessageResponse getMessageById(Long id, String username) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensagem não encontrada"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar se é membro do grupo
        if (!groupMemberRepository.existsByGroupAndUser(message.getGroup(), user)) {
            throw new UnauthorizedException("Você não tem acesso a esta mensagem");
        }

        return mapToResponse(message);
    }

    @Transactional
    public void deleteMessage(Long id, String username) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensagem não encontrada"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar se é o remetente ou admin do grupo
        boolean isOwner = message.getSender().getId().equals(user.getId());
        boolean isAdmin = message.getGroup().getAdmin().getId().equals(user.getId());

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("Você não tem permissão para deletar esta mensagem");
        }

        messageRepository.delete(message);
    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .groupId(message.getGroup().getId())
                .groupName(message.getGroup().getName())
                .senderUsername(message.getIsAnonymous() ? "Anônimo" : message.getSender().getUsername())
                .content(message.getContent())
                .isAnonymous(message.getIsAnonymous())
                .timestamp(message.getTimestamp())
                .build();
    }
}
