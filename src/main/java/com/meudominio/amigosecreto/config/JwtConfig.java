package com.meudominio.amigosecreto.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
@Slf4j
public class JwtConfig {

    private static final String DEFAULT_INSECURE_KEY = "Sua-Chave-Aqui";

    private String secretKey = DEFAULT_INSECURE_KEY;
    private long expiration = 86400000; // 24 horas em milissegundos
    private long refreshExpiration = 604800000; // 7 dias em milissegundos
    private String tokenPrefix = "Bearer ";
    private String headerString = "Authorization";

    @PostConstruct
    public void validate() {
        if (DEFAULT_INSECURE_KEY.equals(secretKey)) {
            log.warn("!@!  JWT secret key está usando o valor padrão inseguro! " +
                     "Defina a variável de ambiente JWT_SECRET antes de subir em produção.");
        }
    }
}
