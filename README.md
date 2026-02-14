# 🎁🎄 API Amigo Secreto 2.0

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-blue.svg)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-5-green.svg)](https://site.mockito.org/)
[![JaCoCo](https://img.shields.io/badge/Coverage-JaCoCo-red.svg)](https://www.jacoco.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

API REST moderna e completa para gerenciar sorteios de amigo secreto de forma digital e automatizada. Desenvolvida com Spring Boot, oferece recursos avançados de gerenciamento de grupos, algoritmo de sorteio inteligente com suporte a bloqueios, sistema de mensagens anônimas e cobertura de testes unitários com JUnit 5 e Mockito.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Executando o Projeto](#executando-o-projeto)
- [Documentação da API](#documentação-da-api)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Testes](#testes)
- [Deploy](#deploy)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

## 🎯 Sobre o Projeto

A API Amigo Secreto 2.0 é uma solução backend robusta que permite a criação e gerenciamento de sorteios de amigo secreto. O sistema foi projetado para ser seguro, escalável e fácil de usar, oferecendo uma experiência completa tanto para organizadores quanto para participantes.

### Por que usar esta API?

- ✅ **Segurança**: Autenticação JWT e criptografia de senhas com BCrypt
- ✅ **Flexibilidade**: Sistema de bloqueios para evitar combinações indesejadas
- ✅ **Privacidade**: Mensagens anônimas entre participantes
- ✅ **Inteligência**: Algoritmo de sorteio com Fisher-Yates shuffle e resolução de bloqueios via permutação cíclica
- ✅ **Qualidade**: Suite de testes unitários com 70+ casos cobrindo happy paths, sad paths e edge cases
- ✅ **Documentação**: Swagger/OpenAPI integrado para fácil utilização

## ⚡ Funcionalidades

### Autenticação e Autorização
- Registro e login de usuários
- Autenticação via JWT (JSON Web Tokens)
- Refresh tokens para renovação automática
- Dois níveis de acesso: ADMIN e PARTICIPANT

### Gerenciamento de Grupos
- Criação de grupos com nome, descrição e data do sorteio
- Adição e remoção de membros
- Sistema de permissões (apenas admin do grupo pode gerenciar)
- Visualização de grupos do usuário

### Sistema de Sorteio
- Algoritmo inteligente com embaralhamento Fisher-Yates e permutação cíclica
- Sistema de bloqueios (usuário pode bloquear quem não quer tirar)
- Resolução automática de conflitos de bloqueio via swap de posições
- Mínimo de 3 participantes obrigatório para executar o sorteio
- Possibilidade de resetar e refazer o sorteio
- Visualização individual do resultado (apenas o usuário vê quem tirou)

### Mensagens Anônimas
- Envio de mensagens para o grupo
- Opção de mensagem anônima ou identificada
- Histórico de mensagens do grupo ordenado por data
- Apenas remetente ou admin pode deletar mensagens

## 🚀 Tecnologias Utilizadas

### Backend
- **Java 17** - Linguagem de programação
- **Spring Boot 3.2.0** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM (Object-Relational Mapping)

### Segurança
- **JWT (jjwt 0.11.5)** - Tokens de autenticação
- **BCrypt** - Criptografia de senhas

### Banco de Dados
- **H2 Database** - Banco em memória para desenvolvimento
- **PostgreSQL** - Recomendado para produção
- **MySQL** - Alternativa para produção

### Documentação
- **SpringDoc OpenAPI 3** - Documentação automática da API
- **Swagger UI** - Interface visual para testar endpoints

### Testes
- **JUnit 5** - Framework de testes unitários
- **Mockito** - Mock de dependências
- **AssertJ** - Assertions fluentes
- **Spring Security Test** - Testes de segurança
- **REST Assured** - Testes de integração de API
- **JaCoCo** - Cobertura de código

### Ferramentas
- **Lombok** - Redução de código boilerplate
- **Maven** - Gerenciamento de dependências

## 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java JDK 17** ou superior
- **Maven 3.8+** (ou usar o wrapper incluído)
- **Git** para clonar o repositório
- **IDE** de sua preferência (IntelliJ IDEA, Eclipse, VS Code)

## 🔧 Instalação

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/API-AmigoSecreto2.0-Natal.git
cd API-AmigoSecreto2.0-Natal
```

2. Compile o projeto:
```bash
./mvnw clean install
```

Ou no Windows:
```bash
mvnw.cmd clean install
```

## ⚙️ Configuração

### Ambiente de Desenvolvimento

O projeto já vem configurado para desenvolvimento com banco H2 em memória. Não é necessário configuração adicional.

### Ambiente de Produção

1. Crie um arquivo `application-prod.properties` em `src/main/resources/`:

```properties
# Database PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/amigosecreto
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Secret (IMPORTANTE: Use uma chave forte em produção!)
jwt.secret-key=${JWT_SECRET}

# Desabilitar console H2
spring.h2.console.enabled=false

# Logs
logging.level.root=WARN
logging.level.com.meudominio.amigosecreto=INFO
```

2. Defina a variável de ambiente `JWT_SECRET`:
```bash
export JWT_SECRET="sua-chave-super-secreta-de-no-minimo-256-bits"
```

## 🏃 Executando o Projeto

### Modo Desenvolvimento

```bash
./mvnw spring-boot:run
```

Ou com perfil específico:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Modo Produção

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Executando o JAR

```bash
java -jar target/amigo-secreto-api-2.0.0.jar --spring.profiles.active=prod
```

A aplicação estará disponível em: `http://localhost:8080`

## 📚 Documentação da API

### Swagger UI

Após iniciar a aplicação, acesse:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Console H2 (Desenvolvimento)

Para acessar o console do banco H2:
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:amigosecretodb`
- **Username**: `sa`
- **Password**: (deixe em branco)

### Principais Endpoints

#### Autenticação
```
POST   /api/auth/register     - Registrar novo usuário
POST   /api/auth/login        - Fazer login
POST   /api/auth/refresh      - Renovar token
```

#### Usuários
```
GET    /api/users/me          - Dados do usuário atual
GET    /api/users             - Listar todos (ADMIN)
GET    /api/users/{id}        - Obter usuário por ID
DELETE /api/users/{id}        - Deletar usuário (ADMIN)
```

#### Grupos
```
POST   /api/groups                    - Criar grupo
GET    /api/groups                    - Listar grupos do usuário
GET    /api/groups/{id}               - Detalhes do grupo
POST   /api/groups/{id}/members       - Adicionar membro
DELETE /api/groups/{id}/members/{uid} - Remover membro
DELETE /api/groups/{id}               - Deletar grupo
POST   /api/groups/{id}/block         - Bloquear usuário
```

#### Sorteios
```
POST   /api/groups/{id}/draw       - Executar sorteio
GET    /api/groups/{id}/my-draw    - Ver meu amigo secreto
GET    /api/groups/{id}/draw/all   - Ver todos os sorteios (ADMIN)
DELETE /api/groups/{id}/draw       - Resetar sorteio
```

#### Mensagens
```
POST   /api/messages               - Enviar mensagem
GET    /api/messages/group/{id}    - Mensagens do grupo
GET    /api/messages/{id}          - Obter mensagem
DELETE /api/messages/{id}          - Deletar mensagem
```

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/meudominio/amigosecreto/
│   │   ├── config/              # Configurações (Security, JWT, OpenAPI)
│   │   ├── controller/          # Controllers REST
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── request/         # DTOs de requisição
│   │   │   └── response/        # DTOs de resposta
│   │   ├── exception/           # Exceções customizadas e handler global
│   │   ├── model/               # Entidades JPA
│   │   │   └── enums/           # Enumerações
│   │   ├── repository/          # Repositórios Spring Data
│   │   ├── security/            # Componentes de segurança (JWT filter, UserDetails)
│   │   ├── service/             # Lógica de negócio
│   │   └── AmigoSecretoApplication.java
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       ├── application-prod.properties
│       └── banner.txt
└── test/
    └── java/com/meudominio/amigosecreto/
        ├── exception/           # Testes do GlobalExceptionHandler
        ├── security/            # Testes do JwtTokenProvider
        └── service/             # Testes de AuthService, GroupService, DrawService,
                                 #   MessageService e UserService
```

## 🧪 Testes

O projeto conta com uma suíte completa de **70+ testes unitários** cobrindo os principais fluxos da aplicação.

### Cobertura por Classe

| Classe | Testes | Cenários cobertos |
|--------|--------|-------------------|
| `AuthService` | 8 | Registro, login, refresh token (sucesso e falhas) |
| `GroupService` | 14 | CRUD de grupos, membros e bloqueios |
| `DrawService` | 12 | Sorteio, consulta de resultado, reset e validações |
| `MessageService` | 11 | Envio, leitura e exclusão de mensagens |
| `UserService` | 7 | Busca, listagem e exclusão de usuários |
| `JwtTokenProvider` | 8 | Geração, validação e extração de tokens |
| `GlobalExceptionHandler` | 10 | Mapeamento de exceções para status HTTP |

### Executar todos os testes

```bash
./mvnw test
```

### Executar uma classe específica

```bash
./mvnw test -Dtest=AuthServiceTest
./mvnw test -Dtest=DrawServiceTest
```

### Executar com cobertura de código

```bash
./mvnw clean test jacoco:report
```

O relatório será gerado em: `target/site/jacoco/index.html`

### Testes de Integração

```bash
./mvnw verify
```

### Padrões utilizados nos testes

- `@ExtendWith(MockitoExtension.class)` — sem contexto Spring, execução rápida
- `@Mock` para dependências, `@InjectMocks` para a classe testada
- `@BeforeEach` para setup de dados comuns
- AssertJ para assertions fluentes
- Padrão AAA (Arrange-Act-Assert)
- `verify(..., never())` para garantir que operações de escrita não ocorrem em caminhos de erro

## 🤝 Contribuindo

Contribuições são sempre bem-vindas! Para contribuir:

1. Faça um Fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

### Padrões de Código

- Siga as convenções Java e Spring Boot
- Mantenha o código limpo e bem documentado
- Escreva testes para novas funcionalidades
- Use commits semânticos

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👥 Autor

- **Otávio Guedes** - [GitHub](https://github.com/Otavio2704)

## 📞 Contato

- Linkedin: https://www.linkedin.com/in/otavio-backend2007/
- Meu Website: https://otavio2007-backend.edgeone.app

⭐ Se este projeto foi útil para você, considere dar uma estrela no GitHub!

**Feito com ❤️ e ☕**
