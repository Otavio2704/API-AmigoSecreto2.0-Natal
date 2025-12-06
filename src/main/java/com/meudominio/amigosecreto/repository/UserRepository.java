package com.meudominio.amigosecreto.repository;

import com.meudominio.amigosecreto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca um usuário pelo username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Busca um usuário pelo email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica se existe um usuário com o username informado
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica se existe um usuário com o email informado
     */
    boolean existsByEmail(String email);
}