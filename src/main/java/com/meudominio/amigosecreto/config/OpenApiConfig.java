package com.meudominio.amigosecreto.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "API Amigo Secreto 2.0",
        version = "2.0.0",
        description = "API REST para gerenciamento de sorteios de amigo secreto com autenticação JWT, " +
                      "gerenciamento de grupos, algoritmo de sorteio inteligente e sistema de mensagens anônimas.",
        contact = @Contact(
            name = "Equipe Amigo Secreto",
            email = "contato@amigosecreto.com",
            url = "https://github.com/seu-usuario/amigo-secreto-api"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            description = "Ambiente Local",
            url = "http://localhost:8080"
        ),
        @Server(
            description = "Ambiente de Produção",
            url = "https://api.amigosecreto.com"
        )
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    description = "Informe o token JWT no formato: Bearer {token}",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
