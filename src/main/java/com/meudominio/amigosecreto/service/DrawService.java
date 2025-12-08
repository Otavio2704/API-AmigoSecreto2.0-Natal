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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de gerenciamento de sorteios
 * Algoritmo: Permutação Cíclica com validação de bloqueios
 * Complexidade: O(n) caso médio, O(kn²) pior caso
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DrawService {

    private final DrawRepository drawRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BlockedUserRepository blockedUserRepository;

    private static final int MAX_ATTEMPTS = 1000;
    private static final int MIN_PARTICIPANTS = 3;

    @Transactional
    public List<DrawResponse> executeDraw(Long groupId, String adminUsername) {
        log.info("Iniciando sorteio para grupo ID: {} por usuário: {}", groupId, adminUsername);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        validateAdminPermissions(group, admin);

        if (drawRepository.existsByGroup(group)) {
            throw new BusinessException(
                "Já existe um sorteio para este grupo. Delete o sorteio atual primeiro."
            );
        }

        List<User> members = getGroupMembers(group);
        validateMinimumParticipants(members);
        Map<Long, Set<Long>> blockMap = buildBlockMap(group);

        log.info("Sorteio: {} participantes, {} bloqueios", 
                 members.size(), countTotalBlocks(blockMap));

        List<Draw> draws = performCyclicPermutationDraw(group, members, blockMap);
        drawRepository.saveAll(draws);

        log.info("✅ Sorteio concluído - {} pares gerados", draws.size());

        return draws.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Algoritmo de Permutação Cíclica
     * 1. Embaralha participantes (Fisher-Yates)
     * 2. Cria ciclo onde cada um tira o próximo
     * 3. Valida bloqueios
     * 4. Se inválido, tenta resolver com swaps
     */
    private List<Draw> performCyclicPermutationDraw(Group group, List<User> members, 
                                                     Map<Long, Set<Long>> blockMap) {
        int n = members.size();
        int attempt = 0;

        while (attempt < MAX_ATTEMPTS) {
            attempt++;
            
            // Embaralha uniformemente
            List<User> shuffled = new ArrayList<>(members);
            fisherYatesShuffle(shuffled);

            // Cria permutação cíclica e valida
            List<Draw> draws = new ArrayList<>();
            boolean isValid = true;

            for (int i = 0; i < n; i++) {
                User giver = shuffled.get(i);
                User receiver = shuffled.get((i + 1) % n);

                if (isBlocked(giver, receiver, blockMap)) {
                    isValid = false;
                    break;
                }

                draws.add(buildDraw(group, giver, receiver));
            }

            if (isValid) {
                log.info("✓ Configuração válida na tentativa #{}", attempt);
                return draws;
            }

            // Tenta resolver com swaps a cada 100 tentativas
            if (attempt % 100 == 0) {
                log.debug("Tentando resolver com trocas (tentativa {})", attempt);
                List<Draw> resolvedDraws = tryResolveWithSwaps(group, shuffled, blockMap);
                
                if (resolvedDraws != null) {
                    log.info("✓ Resolvido com trocas na tentativa #{}", attempt);
                    return resolvedDraws;
                }
            }
        }

        throw new BusinessException(
            String.format(
                "Não foi possível realizar o sorteio após %d tentativas. " +
                "Reduza os bloqueios ou adicione mais participantes.",
                MAX_ATTEMPTS
            )
        );
    }

    // Embaralhamento Fisher-Yates - O(n)
    private void fisherYatesShuffle(List<User> list) {
        Random random = new Random();
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Collections.swap(list, i, j);
        }
    }

    // Tenta resolver bloqueios com trocas de posições - O(n²)
    private List<Draw> tryResolveWithSwaps(Group group, List<User> members, 
                                            Map<Long, Set<Long>> blockMap) {
        int n = members.size();
        List<User> current = new ArrayList<>(members);
        
        int maxSwaps = n * n;
        for (int swaps = 0; swaps < maxSwaps; swaps++) {
            boolean allValid = true;
            
            for (int i = 0; i < n; i++) {
                User giver = current.get(i);
                User receiver = current.get((i + 1) % n);
                
                if (isBlocked(giver, receiver, blockMap)) {
                    boolean resolved = false;
                    
                    // Tenta trocar com outras posições
                    for (int j = 0; j < n; j++) {
                        if (i != j && (i + 1) % n != j) {
                            Collections.swap(current, (i + 1) % n, j);
                            
                            User newReceiver = current.get((i + 1) % n);
                            if (!isBlocked(giver, newReceiver, blockMap)) {
                                if (isSwapValid(current, blockMap)) {
                                    resolved = true;
                                    break;
                                }
                            }
                            
                            Collections.swap(current, (i + 1) % n, j); // Desfaz
                        }
                    }
                    
                    if (!resolved) {
                        allValid = false;
                        break;
                    }
                }
            }
            
            if (allValid) {
                return buildDrawsFromList(group, current);
            }
        }
        
        return null;
    }

    // Valida se configuração não tem bloqueios
    private boolean isSwapValid(List<User> members, Map<Long, Set<Long>> blockMap) {
        int n = members.size();
        for (int i = 0; i < n; i++) {
            User giver = members.get(i);
            User receiver = members.get((i + 1) % n);
            if (isBlocked(giver, receiver, blockMap)) {
                return false;
            }
        }
        return true;
    }

    // Constrói draws a partir da lista ordenada
    private List<Draw> buildDrawsFromList(Group group, List<User> members) {
        List<Draw> draws = new ArrayList<>();
        int n = members.size();
        
        for (int i = 0; i < n; i++) {
            draws.add(buildDraw(group, members.get(i), members.get((i + 1) % n)));
        }
        
        return draws;
    }

    // Constrói mapa de bloqueios para acesso O(1)
    private Map<Long, Set<Long>> buildBlockMap(Group group) {
        List<BlockedUser> blockedUsers = blockedUserRepository.findByGroup(group);
        Map<Long, Set<Long>> blockMap = new HashMap<>();
        
        for (BlockedUser bu : blockedUsers) {
            blockMap.computeIfAbsent(bu.getBlocker().getId(), k -> new HashSet<>())
                    .add(bu.getBlocked().getId());
        }
        
        return blockMap;
    }

    // Verifica bloqueio - O(1)
    private boolean isBlocked(User giver, User receiver, Map<Long, Set<Long>> blockMap) {
        Set<Long> blockedIds = blockMap.get(giver.getId());
        return blockedIds != null && blockedIds.contains(receiver.getId());
    }

    private void validateAdminPermissions(Group group, User admin) {
        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException(
                "Apenas o administrador pode executar o sorteio"
            );
        }
    }

    private void validateMinimumParticipants(List<User> members) {
        if (members.size() < MIN_PARTICIPANTS) {
            throw new BusinessException(
                String.format(
                    "É necessário pelo menos %d participantes. Grupo possui %d.",
                    MIN_PARTICIPANTS, members.size()
                )
            );
        }
    }

    private List<User> getGroupMembers(Group group) {
        return groupMemberRepository.findByGroup(group).stream()
                .map(gm -> gm.getUser())
                .collect(Collectors.toList());
    }

    private int countTotalBlocks(Map<Long, Set<Long>> blockMap) {
        return blockMap.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    private Draw buildDraw(Group group, User giver, User receiver) {
        return Draw.builder()
                .group(group)
                .giver(giver)
                .receiver(receiver)
                .build();
    }

    public DrawResponse getMyDraw(Long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new UnauthorizedException("Você não é membro deste grupo");
        }

        Draw draw = drawRepository.findByGroupAndGiver(group, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Sorteio ainda não foi realizado"
                ));

        return mapToResponse(draw);
    }

    public List<DrawResponse> getAllDraws(Long groupId, String adminUsername) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException(
                "Apenas o administrador pode ver todos os sorteios"
            );
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

        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException(
                "Apenas o administrador pode resetar o sorteio"
            );
        }

        drawRepository.deleteByGroup(group);
        log.info("Sorteio resetado para grupo ID: {} por usuário: {}", groupId, adminUsername);
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
