package com.meudominio.amigosecreto.service;

import com.meudominio.amigosecreto.dto.response.DrawResponse;
import com.meudominio.amigosecreto.exception.BusinessException;
import com.meudominio.amigosecreto.exception.ResourceNotFoundException;
import com.meudominio.amigosecreto.exception.UnauthorizedException;
import com.meudominio.amigosecreto.model.*;
import com.meudominio.amigosecreto.model.enums.Role;
import com.meudominio.amigosecreto.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DrawService - Testes Unitários")
class DrawServiceTest {

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private BlockedUserRepository blockedUserRepository;

    @InjectMocks
    private DrawService drawService;

    private User admin;
    private User user1;
    private User user2;
    private User user3;
    private Group group;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).username("admin").role(Role.ADMIN).build();
        user1 = User.builder().id(2L).username("user1").role(Role.PARTICIPANT).build();
        user2 = User.builder().id(3L).username("user2").role(Role.PARTICIPANT).build();
        user3 = User.builder().id(4L).username("user3").role(Role.PARTICIPANT).build();

        group = Group.builder()
                .id(1L)
                .name("Amigo Secreto 2025")
                .admin(admin)
                .drawDate(LocalDate.of(2025, 12, 20))
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ========================
    // EXECUTE DRAW
    // ========================

    @Test
    @DisplayName("executeDraw - deve realizar sorteio com sucesso")
    void executeDraw_deveRealizarSorteioComSucesso() {
        List<GroupMember> members = List.of(
                buildMember(admin),
                buildMember(user1),
                buildMember(user2),
                buildMember(user3)
        );

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(drawRepository.existsByGroup(group)).thenReturn(false);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(groupMemberRepository.findByGroup(group)).thenReturn(members);
        when(blockedUserRepository.findByGroup(group)).thenReturn(List.of());
        when(drawRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<DrawResponse> results = drawService.executeDraw(1L, "admin");

        assertThat(results).hasSize(4);
        verify(drawRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("executeDraw - deve lançar exceção quando sorteio já existe")
    void executeDraw_deveLancarExcecaoQuandoSorteioJaExiste() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(drawRepository.existsByGroup(group)).thenReturn(true);

        assertThatThrownBy(() -> drawService.executeDraw(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe um sorteio");

        verify(drawRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("executeDraw - deve lançar exceção quando não é admin")
    void executeDraw_deveLancarExcecaoQuandoNaoEhAdmin() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(drawRepository.existsByGroup(group)).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));

        assertThatThrownBy(() -> drawService.executeDraw(1L, "user1"))
                .isInstanceOf(UnauthorizedException.class);

        verify(drawRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("executeDraw - deve lançar exceção quando há menos de 3 participantes")
    void executeDraw_deveLancarExcecaoComMenosDeTresParticipantes() {
        List<GroupMember> members = List.of(
                buildMember(admin),
                buildMember(user1)  // apenas 2 membros
        );

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(drawRepository.existsByGroup(group)).thenReturn(false);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(groupMemberRepository.findByGroup(group)).thenReturn(members);
        when(blockedUserRepository.findByGroup(group)).thenReturn(List.of());

        assertThatThrownBy(() -> drawService.executeDraw(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pelo menos 3 participantes");
    }

    @Test
    @DisplayName("executeDraw - deve lançar exceção quando grupo não é encontrado")
    void executeDraw_deveLancarExcecaoQuandoGrupoNaoEncontrado() {
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> drawService.executeDraw(99L, "admin"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========================
    // GET MY DRAW
    // ========================

    @Test
    @DisplayName("getMyDraw - deve retornar resultado do sorteio do usuário")
    void getMyDraw_deveRetornarResultadoDoSorteio() {
        Draw draw = Draw.builder()
                .id(1L)
                .group(group)
                .giver(admin)
                .receiver(user1)
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(groupMemberRepository.existsByGroupAndUser(group, admin)).thenReturn(true);
        when(drawRepository.findByGroupAndGiver(group, admin)).thenReturn(Optional.of(draw));

        DrawResponse response = drawService.getMyDraw(1L, "admin");

        assertThat(response).isNotNull();
        assertThat(response.getGiverUsername()).isEqualTo("admin");
        assertThat(response.getReceiverUsername()).isEqualTo("user1");
    }

    @Test
    @DisplayName("getMyDraw - deve lançar exceção quando usuário não é membro")
    void getMyDraw_deveLancarExcecaoQuandoNaoEhMembro() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(groupMemberRepository.existsByGroupAndUser(group, user1)).thenReturn(false);

        assertThatThrownBy(() -> drawService.getMyDraw(1L, "user1"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("getMyDraw - deve lançar exceção quando sorteio não foi realizado")
    void getMyDraw_deveLancarExcecaoQuandoSorteioNaoFoiRealizado() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(groupMemberRepository.existsByGroupAndUser(group, admin)).thenReturn(true);
        when(drawRepository.findByGroupAndGiver(group, admin)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> drawService.getMyDraw(1L, "admin"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========================
    // GET ALL DRAWS
    // ========================

    @Test
    @DisplayName("getAllDraws - deve retornar todos os sorteios para o admin")
    void getAllDraws_deveRetornarTodosOsSorteios() {
        Draw draw1 = Draw.builder().id(1L).group(group).giver(admin).receiver(user1).build();
        Draw draw2 = Draw.builder().id(2L).group(group).giver(user1).receiver(user2).build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(drawRepository.findByGroup(group)).thenReturn(List.of(draw1, draw2));

        List<DrawResponse> results = drawService.getAllDraws(1L, "admin");

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("getAllDraws - deve lançar exceção quando não é admin")
    void getAllDraws_deveLancarExcecaoQuandoNaoEhAdmin() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));

        assertThatThrownBy(() -> drawService.getAllDraws(1L, "user1"))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ========================
    // RESET DRAW
    // ========================

    @Test
    @DisplayName("resetDraw - deve resetar sorteio com sucesso")
    void resetDraw_deveResetarSorteioComSucesso() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        drawService.resetDraw(1L, "admin");

        verify(drawRepository).deleteByGroup(group);
    }

    @Test
    @DisplayName("resetDraw - deve lançar exceção quando não é admin")
    void resetDraw_deveLancarExcecaoQuandoNaoEhAdmin() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));

        assertThatThrownBy(() -> drawService.resetDraw(1L, "user1"))
                .isInstanceOf(UnauthorizedException.class);

        verify(drawRepository, never()).deleteByGroup(any());
    }

    // ========================
    // HELPER
    // ========================

    private GroupMember buildMember(User user) {
        return GroupMember.builder().group(group).user(user).build();
    }
}
