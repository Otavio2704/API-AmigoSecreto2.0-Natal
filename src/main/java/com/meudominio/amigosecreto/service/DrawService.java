package com.meudominio.amigosecreto.service;

import com.meudominio.amigosecreto.dto.response.DrawResponse;
import com.meudominio.amigosecreto.exception.BusinessException;
import com.meudominio.amigosecreto.exception.ResourceNotFoundException;
import com.meudominio.amigosecreto.exception.UnauthorizedException;
import com.meudominio.amigosecreto.model.BlockedUser;
import com.meudominio.amigosecreto.model.Draw;
import com.meudominio.amigosecreto.model.Group;
import com.meudominio.amigosecreto.model.User;
import com.meudominio.amigosecreto.repository.BlockedUserRepository;
import com.meudominio.amigosecreto.repository.DrawRepository;
import com.meudominio.amigosecreto.repository.GroupMemberRepository;
import com.meudominio.amigosecreto.repository.GroupRepository;
import com.meudominio.amigosecreto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrawService {

    private final DrawRepository drawRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BlockedUserRepository blockedUserRepository;

    @Transactional
    public List<DrawResponse> executeDraw(Long groupId, String adminUsername) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar se é admin
        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException("Apenas o administrador pode executar o sorteio");
        }

        // Verificar se já existe sorteio
        if (drawRepository.existsByGroup(group)) {
            throw new BusinessException("Já existe um sorteio para este grupo. Delete o sorteio atual primeiro.");
        }

        // Buscar membros do grupo
        List<User> members = groupMemberRepository.findByGroup(group).stream()
                .map(gm -> gm.getUser())
                .collect(Collectors.toList());

        if (members.size() < 3) {
            throw new BusinessException("É necessário pelo menos 3 participantes para realizar o sorteio");
        }

        // Buscar bloqueios
        List<BlockedUser> blockedUsers = blockedUserRepository.findByGroup(group);
        Map<Long, Set<Long>> blockMap = new HashMap<>();
        for (BlockedUser bu : blockedUsers) {
            blockMap.computeIfAbsent(bu.getBlocker().getId(), k -> new HashSet<>())
                    .add(bu.getBlocked().getId());
        }

        // Executar algoritmo de sorteio
        List<Draw> draws = performDraw(group, members, blockMap);

        // Salvar resultados
        drawRepository.saveAll(draws);

        return draws.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private List<Draw> performDraw(Group group, List<User> members, Map<Long, Set<Long>> blockMap) {
        int maxAttempts = 100;
        int attempt = 0;

        while (attempt < maxAttempts) {
            try {
                List<User> givers = new ArrayList<>(members);
                List<User> receivers = new ArrayList<>(members);
                Collections.shuffle(receivers);

                List<Draw> draws = new ArrayList<>();

                for (User giver : givers) {
                    User receiver = findValidReceiver(giver, receivers, blockMap);
                    if (receiver == null) {
                        throw new IllegalStateException("Não foi possível encontrar combinação válida");
                    }

                    draws.add(Draw.builder()
                            .group(group)
                            .giver(giver)
                            .receiver(receiver)
                            .build());

                    receivers.remove(receiver);
                }

                return draws;
            } catch (IllegalStateException e) {
                attempt++;
            }
        }

        throw new BusinessException("Não foi possível realizar o sorteio após " + maxAttempts + 
                " tentativas. Verifique os bloqueios configurados.");
    }

    private User findValidReceiver(User giver, List<User> receivers, Map<Long, Set<Long>> blockMap) {
        Set<Long> blockedIds = blockMap.getOrDefault(giver.getId(), Collections.emptySet());

        for (User receiver : receivers) {
            // Não pode tirar a si mesmo
            if (receiver.getId().equals(giver.getId())) {
                continue;
            }

            // Verificar se está bloqueado
            if (blockedIds.contains(receiver.getId())) {
                continue;
            }

            return receiver;
        }

        return null;
    }

    public DrawResponse getMyDraw(Long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar se é membro
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new UnauthorizedException("Você não é membro deste grupo");
        }

        Draw draw = drawRepository.findByGroupAndGiver(group, user)
                .orElseThrow(() -> new ResourceNotFoundException("Sorteio ainda não foi realizado"));

        return mapToResponse(draw);
    }

    public List<DrawResponse> getAllDraws(Long groupId, String adminUsername) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar se é admin
        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException("Apenas o administrador pode ver todos os sorteios");
        }

        return drawRepository.findByGroup(group).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void resetDraw(Long groupId, String adminUsername) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verificar se é admin
        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException("Apenas o administrador pode resetar o sorteio");
        }

        drawRepository.deleteByGroup(group);
    }

    private DrawResponse mapToResponse(Draw draw) {
        return DrawResponse.builder()
                .id(draw.getId())
                .groupId(draw.getGroup().getId())
                .groupName(draw.getGroup().getName())
                .giverUsername(draw.getGiver().getUsername())
                .receiverUsername(draw.getReceiver().getUsername())
                .build();
    }
}
