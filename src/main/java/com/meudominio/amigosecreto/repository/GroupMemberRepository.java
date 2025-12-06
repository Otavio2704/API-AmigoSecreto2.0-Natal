package com.meudominio.amigosecreto.repository;

import com.meudominio.amigosecreto.model.Group;
import com.meudominio.amigosecreto.model.GroupMember;
import com.meudominio.amigosecreto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    /**
     * Busca todos os membros de um grupo
     */
    List<GroupMember> findByGroup(Group group);
    
    /**
     * Busca todos os grupos que um usuário participa
     */
    List<GroupMember> findByUser(User user);
    
    /**
     * Busca um membro específico em um grupo
     */
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    
    /**
     * Verifica se um usuário é membro de um grupo
     */
    boolean existsByGroupAndUser(Group group, User user);
    
    /**
     * Conta quantos membros tem em um grupo
     */
    long countByGroup(Group group);
}
