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
import com.meudominio.amigosecreto.repository.BlockedUserRepository;
import com.meudominio.amigosecreto.repository.GroupMemberRepository;
import com.meudominio.amigosecreto.repository.GroupRepository;
import com.meudominio.amigosecreto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final String USER_NOT_FOUND = "Usuário não encontrado";
    private static final String GROUP_NOT_FOUND = "Grupo não encontrado";

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BlockedUserRepository blockedUserRepository;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, String username) {
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Criar grupo
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .admin(admin)
                .drawDate(request.getDrawDate())
                .createdAt(LocalDateTime.now())
                .build();

        groupRepository.save(group);

        // Adicionar admin como membro
        GroupMember adminMember = GroupMember.builder()
                .group(group)
                .user(admin)
                .joinedAt(LocalDateTime.now())
                .build();

        groupMemberRepository.save(adminMember);

        return mapToResponse(group);
    }

    public List<GroupResponse> getUserGroups(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return groupMemberRepository.findByUser(user).stream()
                .map(GroupMember::getGroup)
                .map(this::mapToResponse)
                .toList();
    }

    public GroupResponse getGroupById(Long id, String username) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GROUP_NOT_FOUND));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Verificar se usuário é membro
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new UnauthorizedException("Você não é membro deste grupo");
        }

        return mapToResponse(group);
    }

    @Transactional
    public void addMember(Long groupId, Long userId, String adminUsername) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(GROUP_NOT_FOUND));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Verificar se é admin
        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException("Apenas o administrador pode adicionar membros");
        }

        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Verificar se já é membro
        if (groupMemberRepository.existsByGroupAndUser(group, newMember)) {
            throw new BusinessException("Usuário já é membro do grupo");
        }

        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .user(newMember)
                .joinedAt(LocalDateTime.now())
                .build();

        groupMemberRepository.save(groupMember);
    }

    @Transactional
    public void removeMember(Long groupId, Long userId, String adminUsername) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(GROUP_NOT_FOUND));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Verificar se é admin
        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException("Apenas o administrador pode remover membros");
        }

        User member = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Não pode remover o admin
        if (member.getId().equals(admin.getId())) {
            throw new BusinessException("Não é possível remover o administrador do grupo");
        }

        GroupMember groupMember = groupMemberRepository.findByGroupAndUser(group, member)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado no grupo"));

        groupMemberRepository.delete(groupMember);
    }

    @Transactional
    public void deleteGroup(Long groupId, String adminUsername) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(GROUP_NOT_FOUND));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Verificar se é admin
        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException("Apenas o administrador pode deletar o grupo");
        }

        groupRepository.delete(group);
    }

    @Transactional
    public void blockUser(Long groupId, String blockerUsername, Long blockedUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(GROUP_NOT_FOUND));

        User blocker = userRepository.findByUsername(blockerUsername)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        User blocked = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário bloqueado não encontrado"));

        // Verificar se ambos são membros
        if (!groupMemberRepository.existsByGroupAndUser(group, blocker)) {
            throw new UnauthorizedException("Você não é membro deste grupo");
        }

        if (!groupMemberRepository.existsByGroupAndUser(group, blocked)) {
            throw new BusinessException("Usuário a bloquear não é membro do grupo");
        }

        // Verificar se já existe bloqueio
        if (blockedUserRepository.existsByGroupAndBlockerAndBlocked(group, blocker, blocked)) {
            throw new BusinessException("Bloqueio já existe");
        }

        BlockedUser blockedUser = BlockedUser.builder()
                .group(group)
                .blocker(blocker)
                .blocked(blocked)
                .build();

        blockedUserRepository.save(blockedUser);
    }

    private GroupResponse mapToResponse(Group group) {
        List<String> memberNames = groupMemberRepository.findByGroup(group).stream()
                .map(gm -> gm.getUser().getUsername())
                .toList();

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .adminUsername(group.getAdmin().getUsername())
                .drawDate(group.getDrawDate())
                .memberCount(memberNames.size())
                .members(memberNames)
                .createdAt(group.getCreatedAt())
                .build();
    }
}
