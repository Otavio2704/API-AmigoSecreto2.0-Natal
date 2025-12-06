package com.meudominio.amigosecreto.repository;

import com.meudominio.amigosecreto.model.Group;
import com.meudominio.amigosecreto.model.Message;
import com.meudominio.amigosecreto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Busca todas as mensagens de um grupo, ordenadas por timestamp decrescente
     */
    List<Message> findByGroupOrderByTimestampDesc(Group group);
    
    /**
     * Busca todas as mensagens enviadas por um usuário
     */
    List<Message> findBySender(User sender);
    
    /**
     * Busca todas as mensagens de um grupo enviadas por um usuário específico
     */
    List<Message> findByGroupAndSender(Group group, User sender);
    
    /**
     * Busca apenas mensagens anônimas de um grupo
     */
    List<Message> findByGroupAndIsAnonymousTrueOrderByTimestampDesc(Group group);
    
    /**
     * Busca apenas mensagens não anônimas de um grupo
     */
    List<Message> findByGroupAndIsAnonymousFalseOrderByTimestampDesc(Group group);
    
    /**
     * Conta quantas mensagens existem em um grupo
     */
    long countByGroup(Group group);
    
    /**
     * Deleta todas as mensagens de um grupo
     */
    void deleteByGroup(Group group);
}
