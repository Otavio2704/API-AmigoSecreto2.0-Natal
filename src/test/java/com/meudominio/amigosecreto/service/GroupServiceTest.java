package com.meudominio.amigosecreto.service;

import com.meudominio.amigosecreto.dto.request.CreateGroupRequest;
import com.meudominio.amigosecreto.dto.response.GroupResponse;
import com.meudominio.amigosecreto.exception.BusinessException;
import com.meudominio.amigosecreto.exception.ResourceNotFoundException;
import com.meudominio.amigosecreto.exception.UnauthorizedException;
import com.meudominio.amigosecreto.model.BlockedUser;
import com.meudominio.amigosecreto.model.Group;
import com.meudominio.amigosecreto.model.GroupMember;
import com.meudominio.amigosecreto.model.User;
import com.meudominio.amigosecreto.model.enums.Role;
import com.meudominio.amigosecreto.repository.BlockedUserRepository;
import com.meudominio.amigosecreto.repository.GroupMemberRepository;
import com.meudominio.amigosecreto.repository.GroupRepository;
import com.meudominio.amigosecreto.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupService - Testes Unitários")
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private BlockedUserRepository blockedUserRepository;

    @InjectMocks
    private GroupService groupService;

    private User admin;
    private User member;
    private Group group;
    private CreateGroupRequest createGroupRequest;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@email.com")
                .role(Role.ADMIN)
                .build();

        member = User.builder()
                .id(2L)
                .username("membro")
                .email("membro@email.com")
                .role(Role.PARTICIPANT)
                .build();

        group = Group.builder()
                .id(1L)
                .name("Amigo Secreto 2025")
                .description("Confraternização")
                .admin(admin)
                .drawDate(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .build();

        createGroupRequest = CreateGroupRequest.builder()
                .name("Amigo Secreto 2025")
                .description("Confraternização")
                .drawDate(LocalDate.now().plusDays(30))
                .build();
    }

    // ========================
    // CRIAR GRUPO
    // ========================

    @Test
    @DisplayName("createGroup - deve criar grupo com sucesso e adicionar admin como membro")
    void createGroup_deveCriarGrupoComSucesso() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(groupRepository.save(any(Group.class))).thenReturn(group);
        when(groupMemberRepository.save(any(GroupMember.class))).thenReturn(new GroupMember());
        when(groupMemberRepository.findByGroup(any())).thenReturn(List.of());

        GroupResponse response = groupService.createGroup(createGroupRequest, "admin");

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Amigo Secreto 2025");
        assertThat(response.getAdminUsername()).isEqualTo("admin");

        verify(groupRepository).save(any(Group.class));
        verify(groupMemberRepository).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("createGroup - deve lançar exceção quando usuário não encontrado")
    void createGroup_deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(userRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.createGroup(createGroupRequest, "inexistente"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(groupRepository, never()).save(any());
    }

    // ========================
    // PEGAR OS USUÁRIOS DOS GRUPOS
    // ========================

    @Test
    @DisplayName("getUserGroups - deve retornar grupos do usuário")
    void getUserGroups_deveRetornarGruposDoUsuario() {
        GroupMember membership = GroupMember.builder().group(group).user(admin).build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(groupMemberRepository.findByUser(admin)).thenReturn(List.of(membership));
        when(groupMemberRepository.findByGroup(group)).thenReturn(List.of(membership));

        List<GroupResponse> groups = groupService.getUserGroups("admin");

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).getName()).isEqualTo("Amigo Secreto 2025");
    }

    @Test
    @DisplayName("getUserGroups - deve retornar lista vazia quando usuário não tem grupos")
    void getUserGroups_deveRetornarListaVazia() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(groupMemberRepository.findByUser(admin)).thenReturn(List.of());

        List<GroupResponse> groups = groupService.getUserGroups("admin");

        assertThat(groups).isEmpty();
    }

    // ========================
    // PEGAR GRUPO POR ID
    // ========================

    @Test
    @DisplayName("getGroupById - deve retornar grupo quando usuário é membro")
    void getGroupById_deveRetornarGrupoQuandoEhMembro() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(groupMemberRepository.existsByGroupAndUser(group, admin)).thenReturn(true);
        when(groupMemberRepository.findByGroup(group)).thenReturn(List.of());

        GroupResponse response = groupService.getGroupById(1L, "admin");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getGroupById - deve lançar exceção quando usuário não é membro")
    void getGroupById_deveLancarExcecaoQuandoNaoEhMembro() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("membro")).thenReturn(Optional.of(member));
        when(groupMemberRepository.existsByGroupAndUser(group, member)).thenReturn(false);

        assertThatThrownBy(() -> groupService.getGroupById(1L, "membro"))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ========================
    // ADICIONAR MEMBRO
    // ========================

    @Test
    @DisplayName("addMember - deve adicionar membro com sucesso")
    void addMember_deveAdicionarMembroComSucesso() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.existsByGroupAndUser(group, member)).thenReturn(false);

        groupService.addMember(1L, 2L, "admin");

        verify(groupMemberRepository).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("addMember - deve lançar exceção quando não é admin")
    void addMember_deveLancarExcecaoQuandoNaoEhAdmin() {
        User outroUsuario = User.builder().id(3L).username("outro").build();
        group = Group.builder().id(1L).admin(admin).name("Grupo").createdAt(LocalDateTime.now()).build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("outro")).thenReturn(Optional.of(outroUsuario));

        assertThatThrownBy(() -> groupService.addMember(1L, 2L, "outro"))
                .isInstanceOf(UnauthorizedException.class);

        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("addMember - deve lançar exceção quando usuário já é membro")
    void addMember_deveLancarExcecaoQuandoJaEhMembro() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.existsByGroupAndUser(group, member)).thenReturn(true);

        assertThatThrownBy(() -> groupService.addMember(1L, 2L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Usuário já é membro do grupo");
    }

    // ========================
    // REMOVER MEMNBRO
    // ========================

    @Test
    @DisplayName("removeMember - deve remover membro com sucesso")
    void removeMember_deveRemoverMembroComSucesso() {
        GroupMember membership = GroupMember.builder().group(group).user(member).build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.findByGroupAndUser(group, member)).thenReturn(Optional.of(membership));

        groupService.removeMember(1L, 2L, "admin");

        verify(groupMemberRepository).delete(membership);
    }

    @Test
    @DisplayName("removeMember - deve lançar exceção ao tentar remover o admin")
    void removeMember_deveLancarExcecaoAoTentarRemoverAdmin() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> groupService.removeMember(1L, 1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Não é possível remover o administrador do grupo");
    }

    // ========================
    // DELETAR GRUPO
    // ========================

    @Test
    @DisplayName("deleteGroup - deve deletar grupo com sucesso")
    void deleteGroup_deveDeletarGrupoComSucesso() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        groupService.deleteGroup(1L, "admin");

        verify(groupRepository).delete(group);
    }

    @Test
    @DisplayName("deleteGroup - deve lançar exceção quando não é admin")
    void deleteGroup_deveLancarExcecaoQuandoNaoEhAdmin() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("membro")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> groupService.deleteGroup(1L, "membro"))
                .isInstanceOf(UnauthorizedException.class);

        verify(groupRepository, never()).delete(any());
    }

    // ========================
    // BLOQUEAR USUÁRIO
    // ========================

    @Test
    @DisplayName("blockUser - deve bloquear usuário com sucesso")
    void blockUser_deveBloquearUsuarioComSucesso() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.existsByGroupAndUser(group, admin)).thenReturn(true);
        when(groupMemberRepository.existsByGroupAndUser(group, member)).thenReturn(true);
        when(blockedUserRepository.existsByGroupAndBlockerAndBlocked(group, admin, member)).thenReturn(false);

        groupService.blockUser(1L, "admin", 2L);

        verify(blockedUserRepository).save(any(BlockedUser.class));
    }

    @Test
    @DisplayName("blockUser - deve lançar exceção quando bloqueio já existe")
    void blockUser_deveLancarExcecaoQuandoBloqueioJaExiste() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.existsByGroupAndUser(group, admin)).thenReturn(true);
        when(groupMemberRepository.existsByGroupAndUser(group, member)).thenReturn(true);
        when(blockedUserRepository.existsByGroupAndBlockerAndBlocked(group, admin, member)).thenReturn(true);

        assertThatThrownBy(() -> groupService.blockUser(1L, "admin", 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Bloqueio já existe");
    }

    @Test
    @DisplayName("blockUser - deve lançar exceção quando usuário tenta bloquear a si mesmo")
    void blockUser_deveLancarExcecaoQuandoAutoBloquear() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> groupService.blockUser(1L, "admin", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Você não pode bloquear a si mesmo");

        verify(blockedUserRepository, never()).save(any());
    }
}
