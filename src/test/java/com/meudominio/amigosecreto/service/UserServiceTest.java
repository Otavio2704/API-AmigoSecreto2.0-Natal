package com.meudominio.amigosecreto.service;

import com.meudominio.amigosecreto.dto.response.UserResponse;
import com.meudominio.amigosecreto.exception.ResourceNotFoundException;
import com.meudominio.amigosecreto.model.User;
import com.meudominio.amigosecreto.model.enums.Role;
import com.meudominio.amigosecreto.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Testes Unitários")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("joaosilva")
                .email("joao@email.com")
                .password("senha_encoded")
                .role(Role.PARTICIPANT)
                .build();
    }

    // ========================
    // GET USER BY USERNAME
    // ========================

    @Test
    @DisplayName("getUserByUsername - deve retornar usuário existente")
    void getUserByUsername_deveRetornarUsuario() {
        when(userRepository.findByUsername("joaosilva")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserByUsername("joaosilva");

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("joaosilva");
        assertThat(response.getEmail()).isEqualTo("joao@email.com");
        assertThat(response.getRole()).isEqualTo("PARTICIPANT");
    }

    @Test
    @DisplayName("getUserByUsername - deve lançar exceção quando usuário não existe")
    void getUserByUsername_deveLancarExcecaoQuandoNaoExiste() {
        when(userRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("inexistente"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========================
    // GET USER BY ID
    // ========================

    @Test
    @DisplayName("getUserById - deve retornar usuário existente")
    void getUserById_deveRetornarUsuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("joaosilva");
    }

    @Test
    @DisplayName("getUserById - deve lançar exceção quando ID não existe")
    void getUserById_deveLancarExcecaoQuandoNaoExiste() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========================
    // GET ALL USERS
    // ========================

    @Test
    @DisplayName("getAllUsers - deve retornar todos os usuários")
    void getAllUsers_deveRetornarTodosUsuarios() {
        User user2 = User.builder()
                .id(2L).username("mariasantos").email("maria@email.com").role(Role.PARTICIPANT).build();

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserResponse> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserResponse::getUsername)
                .containsExactlyInAnyOrder("joaosilva", "mariasantos");
    }

    @Test
    @DisplayName("getAllUsers - deve retornar lista vazia quando não há usuários")
    void getAllUsers_deveRetornarListaVazia() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> users = userService.getAllUsers();

        assertThat(users).isEmpty();
    }

    // ========================
    // DELETE USER
    // ========================

    @Test
    @DisplayName("deleteUser - deve deletar usuário com sucesso")
    void deleteUser_deveDeletarUsuarioComSucesso() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser - deve lançar exceção quando usuário não existe")
    void deleteUser_deveLancarExcecaoQuandoNaoExiste() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
