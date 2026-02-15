package com.meudominio.amigosecreto.service;

import com.meudominio.amigosecreto.dto.request.SendMessageRequest;
import com.meudominio.amigosecreto.dto.response.MessageResponse;
import com.meudominio.amigosecreto.exception.ResourceNotFoundException;
import com.meudominio.amigosecreto.exception.UnauthorizedException;
import com.meudominio.amigosecreto.model.Group;
import com.meudominio.amigosecreto.model.Message;
import com.meudominio.amigosecreto.model.User;
import com.meudominio.amigosecreto.model.enums.Role;
import com.meudominio.amigosecreto.repository.GroupMemberRepository;
import com.meudominio.amigosecreto.repository.GroupRepository;
import com.meudominio.amigosecreto.repository.MessageRepository;
import com.meudominio.amigosecreto.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService - Testes Unitários")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private MessageService messageService;

    private User admin;
    private User sender;
    private Group group;
    private Message message;
    private SendMessageRequest sendRequest;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).username("admin").role(Role.ADMIN).build();
        sender = User.builder().id(2L).username("sender").role(Role.PARTICIPANT).build();

        group = Group.builder()
                .id(1L)
                .name("Amigo Secreto 2025")
                .admin(admin)
                .createdAt(LocalDateTime.now())
                .build();

        message = Message.builder()
                .id(1L)
                .group(group)
                .sender(sender)
                .content("Olá pessoal!")
                .isAnonymous(true)
                .timestamp(LocalDateTime.now())
                .build();

        sendRequest = SendMessageRequest.builder()
                .groupId(1L)
                .content("Olá pessoal!")
                .isAnonymous(true)
                .build();
    }

    // ========================
    // ENVIAR MENSAGEM
    // ========================

    @Test
    @DisplayName("sendMessage - deve enviar mensagem anônima com sucesso")
    void sendMessage_deveEnviarMensagemAnonima() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("sender")).thenReturn(Optional.of(sender));
        when(groupMemberRepository.existsByGroupAndUser(group, sender)).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponse response = messageService.sendMessage(sendRequest, "sender");

        assertThat(response).isNotNull();
        assertThat(response.getIsAnonymous()).isTrue();
        assertThat(response.getSenderUsername()).isEqualTo("Anônimo");

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("sendMessage - deve enviar mensagem identificada com sucesso")
    void sendMessage_deveEnviarMensagemIdentificada() {
        Message msgIdentificada = Message.builder()
                .id(2L).group(group).sender(sender)
                .content("Olá!").isAnonymous(false)
                .timestamp(LocalDateTime.now()).build();

        SendMessageRequest reqIdentificada = SendMessageRequest.builder()
                .groupId(1L).content("Olá!").isAnonymous(false).build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("sender")).thenReturn(Optional.of(sender));
        when(groupMemberRepository.existsByGroupAndUser(group, sender)).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenReturn(msgIdentificada);

        MessageResponse response = messageService.sendMessage(reqIdentificada, "sender");

        assertThat(response.getSenderUsername()).isEqualTo("sender");
        assertThat(response.getIsAnonymous()).isFalse();
    }

    @Test
    @DisplayName("sendMessage - deve tratar isAnonymous null como anônimo (comportamento padrão)")
    void sendMessage_deveTratarIsAnonymousNuloComoAnonimo() {
        SendMessageRequest reqSemFlag = SendMessageRequest.builder()
                .groupId(1L)
                .content("Mensagem sem flag")
                .isAnonymous(null)
                .build();

        Message msgAnonima = Message.builder()
                .id(3L).group(group).sender(sender)
                .content("Mensagem sem flag").isAnonymous(true)
                .timestamp(LocalDateTime.now()).build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("sender")).thenReturn(Optional.of(sender));
        when(groupMemberRepository.existsByGroupAndUser(group, sender)).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenReturn(msgAnonima);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);

        messageService.sendMessage(reqSemFlag, "sender");

        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getIsAnonymous()).isTrue();
    }

    @Test
    @DisplayName("sendMessage - deve lançar exceção quando usuário não é membro")
    void sendMessage_deveLancarExcecaoQuandoNaoEhMembro() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("sender")).thenReturn(Optional.of(sender));
        when(groupMemberRepository.existsByGroupAndUser(group, sender)).thenReturn(false);

        assertThatThrownBy(() -> messageService.sendMessage(sendRequest, "sender"))
                .isInstanceOf(UnauthorizedException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendMessage - deve lançar exceção quando grupo não é encontrado")
    void sendMessage_deveLancarExcecaoQuandoGrupoNaoEncontrado() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(sendRequest, "sender"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========================
    // OBTER AS MENSAGENS DO GRUPO
    // ========================

    @Test
    @DisplayName("getGroupMessages - deve retornar mensagens do grupo")
    void getGroupMessages_deveRetornarMensagens() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("sender")).thenReturn(Optional.of(sender));
        when(groupMemberRepository.existsByGroupAndUser(group, sender)).thenReturn(true);
        when(messageRepository.findByGroupOrderByTimestampDesc(group)).thenReturn(List.of(message));

        List<MessageResponse> messages = messageService.getGroupMessages(1L, "sender");

        assertThat(messages).hasSize(1);
    }

    @Test
    @DisplayName("getGroupMessages - deve lançar exceção quando não é membro")
    void getGroupMessages_deveLancarExcecaoQuandoNaoEhMembro() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("sender")).thenReturn(Optional.of(sender));
        when(groupMemberRepository.existsByGroupAndUser(group, sender)).thenReturn(false);

        assertThatThrownBy(() -> messageService.getGroupMessages(1L, "sender"))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ========================
    // PEGAR MENSAGEM POR ID
    // ========================

    @Test
    @DisplayName("getMessageById - deve retornar mensagem quando usuário é membro")
    void getMessageById_deveRetornarMensagem() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(userRepository.findByUsername("sender")).thenReturn(Optional.of(sender));
        when(groupMemberRepository.existsByGroupAndUser(group, sender)).thenReturn(true);

        MessageResponse response = messageService.getMessageById(1L, "sender");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getMessageById - deve lançar exceção quando mensagem não é encontrada")
    void getMessageById_deveLancarExcecaoQuandoNaoEncontrada() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getMessageById(99L, "sender"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========================
    // DELETAR MENSSAGEM
    // ========================

    @Test
    @DisplayName("deleteMessage - remetente deve conseguir deletar sua própria mensagem")
    void deleteMessage_remetentePodeDeletarSuaMensagem() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(userRepository.findByUsername("sender")).thenReturn(Optional.of(sender));

        messageService.deleteMessage(1L, "sender");

        verify(messageRepository).delete(message);
    }

    @Test
    @DisplayName("deleteMessage - admin do grupo deve conseguir deletar qualquer mensagem")
    void deleteMessage_adminPodeDeletarQualquerMensagem() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        messageService.deleteMessage(1L, "admin");

        verify(messageRepository).delete(message);
    }

    @Test
    @DisplayName("deleteMessage - deve lançar exceção quando usuário não tem permissão")
    void deleteMessage_deveLancarExcecaoQuandoNaoTemPermissao() {
        User outro = User.builder().id(5L).username("outro").build();

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(userRepository.findByUsername("outro")).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> messageService.deleteMessage(1L, "outro"))
                .isInstanceOf(UnauthorizedException.class);

        verify(messageRepository, never()).delete(any());
    }
}
