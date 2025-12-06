package com.meudominio.amigosecreto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Classe principal da aplicação Amigo Secreto API 2.0
 * 
 * Solução backend moderna para gerenciar sorteios de amigo secreto
 * de forma digital e automatizada.
 * 
 * Funcionalidades:
 * - Gestão de usuários com diferentes níveis de acesso
 * - Gerenciamento de grupos
 * - Sorteio inteligente (evita auto-sorteios e respeita bloqueios)
 * - Mensagens anônimas entre participantes
 * - Segurança com JWT
 * 
 * @author Seu Nome
 * @version 2.0.0
 * @since 2025
 */
@SpringBootApplication
@EnableJpaRepositories
@Slf4j
public class AmigoSecretoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AmigoSecretoApplication.class, args);
        logApplicationStartup(context.getEnvironment());
    }

    /**
     * Exibe informações sobre a aplicação no console após inicialização
     */
    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        String hostAddress = "localhost";
        
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Não foi possível determinar o endereço do host");
        }

        String profiles = String.join(", ", env.getActiveProfiles());
        if (profiles.isEmpty()) {
            profiles = String.join(", ", env.getDefaultProfiles());
        }

        log.info("\n----------------------------------------------------------\n\t" +
                "Aplicação '{}' está rodando! Acesse:\n\t" +
                "Local:      \t{}://localhost:{}{}\n\t" +
                "Externo:    \t{}://{}:{}{}\n\t" +
                "Profile(s): \t{}\n\t" +
                "Swagger UI: \t{}://localhost:{}/swagger-ui.html\n\t" +
                "API Docs:   \t{}://localhost:{}/v3/api-docs\n" +
                "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                profiles,
                protocol,
                serverPort,
                protocol,
                serverPort
        );
    }
}
