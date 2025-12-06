package com.meudominio.amigosecreto.repository;

import com.meudominio.amigosecreto.model.Draw;
import com.meudominio.amigosecreto.model.Group;
import com.meudominio.amigosecreto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrawRepository extends JpaRepository<Draw, Long> {
    
    /**
     * Busca todos os sorteios de um grupo
     */
    List<Draw> findByGroup(Group group);
    
    /**
     * Busca o sorteio de um participante específico (quem ele tirou)
     */
    Optional<Draw> findByGroupAndGiver(Group group, User giver);
    
    /**
     * Busca quem tirou um determinado usuário
     */
    Optional<Draw> findByGroupAndReceiver(Group group, User receiver);
    
    /**
     * Verifica se já existe sorteio para um grupo
     */
    boolean existsByGroup(Group group);
    
    /**
     * Deleta todos os sorteios de um grupo (para resetar)
     */
    void deleteByGroup(Group group);
}
