package com.meudominio.amigosecreto.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {

    private String secretKey = "Sua-Chave-Aqui";
    private long expiration = 86400000; // 24 horas em milissegundos
    private long refreshExpiration = 604800000; // 7 dias em milissegundos
    private String tokenPrefix = "Bearer ";
    private String headerString = "Authorization";
}
