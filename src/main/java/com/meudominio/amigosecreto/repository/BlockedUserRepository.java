package com.meudominio.amigosecreto.repository;

import com.meudominio.amigosecreto.model.BlockedUser;
import com.meudominio.amigosecreto.model.Group;
import com.meudominio.amigosecreto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {
    
    /**
     * Busca todos os bloqueios de um grupo
     */
    List<BlockedUser> findByGroup(Group group);
    
    /**
     * Busca todos os usuários bloqueados por um usuário específico em um grupo
     */
    List<BlockedUser> findByGroupAndBlocker(Group group, User blocker);
    
    /**
     * Busca todos que bloquearam um usuário específico em um grupo
     */
    List<BlockedUser> findByGroupAndBlocked(Group group, User blocked);
    
    /**
     * Verifica se existe um bloqueio específico
     */
    boolean existsByGroupAndBlockerAndBlocked(Group group, User blocker, User blocked);
    
    /**
     * Deleta todos os bloqueios de um grupo
     */
    void deleteByGroup(Group group);
}
