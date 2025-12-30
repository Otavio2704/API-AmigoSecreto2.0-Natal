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
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Serviço de gerenciamento de sorteios
 * Algoritmo: Permutação Cíclica com validação de bloqueios
 * Complexidade: O(n) caso médio, O(kn²) pior caso
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DrawService {

    // Constantes
    private static final String GROUP_NOT_FOUND = "Grupo não encontrado";
    private static final String USER_NOT_FOUND = "Usuário não encontrado";
    private static final String DRAW_ALREADY_EXISTS = 
        "Já existe um sorteio para este grupo. Delete o sorteio atual primeiro.";
    private static final String UNAUTHORIZED_ADMIN = 
        "Apenas o administrador pode executar o sorteio";
    private static final String UNAUTHORIZED_RESET = 
        "Apenas o administrador pode resetar o sorteio";
    private static final String UNAUTHORIZED_VIEW_ALL = 
        "Apenas o administrador pode ver todos os sorteios";
    private static final String DRAW_NOT_FOUND = "Sorteio ainda não foi realizado";
    private static final String NOT_MEMBER = "Você não é membro deste grupo";
    
    private static final int MAX_ATTEMPTS = 1000;
    private static final int MIN_PARTICIPANTS = 3;
    private static final Random RANDOM = new Random();

    // Repositories
    private final DrawRepository drawRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BlockedUserRepository blockedUserRepository;

    /**
     * Executa o sorteio para um grupo
     */
    @Transactional
    public List<DrawResponse> executeDraw(Long groupId, String adminUsername) {
        log.info("Iniciando sorteio para grupo ID: {} por usuário: {}", groupId, adminUsername);
        
        Group group = findAndValidateGroup(groupId);
        User admin = findAndValidateAdmin(adminUsername, group);
        
        DrawContext context = prepareDrawContext(group);
        List<Draw> draws = executeDrawAlgorithm(context);
        
        return saveAndConvertDraws(draws);
    }

    /**
     * Busca e valida o grupo
     */
    private Group findAndValidateGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(GROUP_NOT_FOUND));

        if (drawRepository.existsByGroup(group)) {
            throw new BusinessException(DRAW_ALREADY_EXISTS);
        }

        return group;
    }

    /**
     * Busca e valida o administrador
     */
    private User findAndValidateAdmin(String username, Group group) {
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        validateAdminPermissions(group, admin);
        return admin;
    }

    /**
     * Prepara o contexto do sorteio
     */
    private DrawContext prepareDrawContext(Group group) {
        List<User> members = getGroupMembers(group);
        validateMinimumParticipants(members);
        Map<Long, Set<Long>> blockMap = buildBlockMap(group);

        log.info("Sorteio: {} participantes, {} bloqueios", 
                 members.size(), countTotalBlocks(blockMap));

        return new DrawContext(group, members, blockMap);
    }

    /**
     * Executa o algoritmo de sorteio
     */
    private List<Draw> executeDrawAlgorithm(DrawContext context) {
        int attempt = 0;

        while (attempt < MAX_ATTEMPTS) {
            attempt++;
            
            List<Draw> draws = tryDrawWithCyclicPermutation(context);
            
            if (draws != null) {
                log.info("✓ Configuração válida na tentativa #{}", attempt);
                return draws;
            }

            // Tenta resolver com swaps a cada 100 tentativas
            if (attempt % 100 == 0) {
                log.debug("Tentando resolver com trocas (tentativa {})", attempt);
                List<Draw> resolvedDraws = tryResolveWithSwaps(context);
                
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

    /**
     * Tenta criar sorteio com permutação cíclica
     */
    private List<Draw> tryDrawWithCyclicPermutation(DrawContext context) {
        int n = context.getMembers().size();
        
        List<User> shuffled = new ArrayList<>(context.getMembers());
        fisherYatesShuffle(shuffled);

        List<Draw> draws = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            User giver = shuffled.get(i);
            User receiver = shuffled.get((i + 1) % n);

            if (isBlocked(giver, receiver, context.getBlockMap())) {
                return Collections.emptyList();
            }

            draws.add(buildDraw(context.getGroup(), giver, receiver));
        }

        return draws;
    }

    /**
     * Salva e converte draws para resposta
     */
    private List<DrawResponse> saveAndConvertDraws(List<Draw> draws) {
        drawRepository.saveAll(draws);
        log.info("Sorteio concluído - {} pares gerados", draws.size());

        return draws.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Embaralhamento Fisher-Yates - O(n)
     */
    private void fisherYatesShuffle(List<User> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            Collections.swap(list, i, j);
        }
    }

    /**
     * Tenta resolver bloqueios com trocas de posições - O(n²)
     */
    private List<Draw> tryResolveWithSwaps(DrawContext context) {
        int n = context.getMembers().size();
        List<User> current = new ArrayList<>(context.getMembers());
        
        int maxSwaps = n * n;
        for (int swaps = 0; swaps < maxSwaps; swaps++) {
            boolean allValid = true;
            
            for (int i = 0; i < n; i++) {
                User giver = current.get(i);
                User receiver = current.get((i + 1) % n);
                
                if (isBlocked(giver, receiver, context.getBlockMap())) {
                    boolean resolved = trySwapToResolveBlock(current, i, n, context.getBlockMap());
                    
                    if (!resolved) {
                        allValid = false;
                        break;
                    }
                }
            }
            
            if (allValid) {
                return buildDrawsFromList(context.getGroup(), current);
            }
        }
        
        return Collections.emptyList();
    }

    /**
     * Tenta trocar posições para resolver bloqueio
     */
    private boolean trySwapToResolveBlock(List<User> current, int i, int n, 
                                          Map<Long, Set<Long>> blockMap) {
        User giver = current.get(i);
        
        for (int j = 0; j < n; j++) {
            if (i != j && (i + 1) % n != j) {
                Collections.swap(current, (i + 1) % n, j);
                
                User newReceiver = current.get((i + 1) % n);
                if (!isBlocked(giver, newReceiver, blockMap) && isSwapValid(current, blockMap)) {
                    return true;
                }
                
                Collections.swap(current, (i + 1) % n, j); // Desfaz
            }
        }
        
        return false;
    }

    /**
     * Valida se configuração não tem bloqueios
     */
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

    /**
     * Constrói draws a partir da lista ordenada
     */
    private List<Draw> buildDrawsFromList(Group group, List<User> members) {
        List<Draw> draws = new ArrayList<>();
        int n = members.size();
        
        for (int i = 0; i < n; i++) {
            draws.add(buildDraw(group, members.get(i), members.get((i + 1) % n)));
        }
        
        return draws;
    }

    /**
     * Constrói mapa de bloqueios para acesso O(1)
     */
    private Map<Long, Set<Long>> buildBlockMap(Group group) {
        List<BlockedUser> blockedUsers = blockedUserRepository.findByGroup(group);
        Map<Long, Set<Long>> blockMap = new HashMap<>();
        
        for (BlockedUser bu : blockedUsers) {
            blockMap.computeIfAbsent(bu.getBlocker().getId(), k -> new HashSet<>())
                    .add(bu.getBlocked().getId());
        }
        
        return blockMap;
    }

    /**
     * Verifica bloqueio - O(1)
     */
    private boolean isBlocked(User giver, User receiver, Map<Long, Set<Long>> blockMap) {
        Set<Long> blockedIds = blockMap.get(giver.getId());
        return blockedIds != null && blockedIds.contains(receiver.getId());
    }

    /**
     * Valida permissões de administrador
     */
    private void validateAdminPermissions(Group group, User admin) {
        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException(UNAUTHORIZED_ADMIN);
        }
    }

    /**
     * Valida número mínimo de participantes
     */
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

    /**
     * Obtém membros do grupo
     */
    private List<User> getGroupMembers(Group group) {
        return groupMemberRepository.findByGroup(group).stream()
                .map(gm -> gm.getUser())
                .toList();
    }

    /**
     * Conta total de bloqueios
     */
    private int countTotalBlocks(Map<Long, Set<Long>> blockMap) {
        return blockMap.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Constrói objeto Draw
     */
    private Draw buildDraw(Group group, User giver, User receiver) {
        return Draw.builder()
                .group(group)
                .giver(giver)
                .receiver(receiver)
                .build();
    }

    /**
     * Retorna o resultado do sorteio do usuário atual
     */
    public DrawResponse getMyDraw(Long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(GROUP_NOT_FOUND));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new UnauthorizedException(NOT_MEMBER);
        }

        Draw draw = drawRepository.findByGroupAndGiver(group, user)
                .orElseThrow(() -> new ResourceNotFoundException(DRAW_NOT_FOUND));

        return mapToResponse(draw);
    }

    /**
     * Retorna todos os resultados do sorteio (apenas admin)
     */
    public List<DrawResponse> getAllDraws(Long groupId, String adminUsername) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(GROUP_NOT_FOUND));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException(UNAUTHORIZED_VIEW_ALL);
        }

        return drawRepository.findByGroup(group).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Reseta o sorteio do grupo
     */
    @Transactional
    public void resetDraw(Long groupId, String adminUsername) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(GROUP_NOT_FOUND));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (!group.getAdmin().getId().equals(admin.getId())) {
            throw new UnauthorizedException(UNAUTHORIZED_RESET);
        }

        drawRepository.deleteByGroup(group);
        log.info("Sorteio resetado para grupo ID: {} por usuário: {}", groupId, adminUsername);
    }

    /**
     * Mapeia Draw para DrawResponse
     */
    private DrawResponse mapToResponse(Draw draw) {
        return DrawResponse.builder()
                .id(draw.getId())
                .groupId(draw.getGroup().getId())
                .groupName(draw.getGroup().getName())
                .giverUsername(draw.getGiver().getUsername())
                .receiverUsername(draw.getReceiver().getUsername())
                .build();
    }

    /**
     * Classe interna para contexto do sorteio
     */
    @Value
    private static class DrawContext {
        Group group;
        List<User> members;
        Map<Long, Set<Long>> blockMap;
    }
}
