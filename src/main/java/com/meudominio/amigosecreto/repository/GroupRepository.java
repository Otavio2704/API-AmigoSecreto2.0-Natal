package com.meudominio.amigosecreto.repository;

import com.meudominio.amigosecreto.model.Group;
import com.meudominio.amigosecreto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    /**
     * Busca todos os grupos onde o usuário é administrador
     */
    List<Group> findByAdmin(User admin);
    
    /**
     * Busca grupos pelo nome (útil para pesquisa)
     */
    List<Group> findByNameContainingIgnoreCase(String name);
}
