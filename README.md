# Auto Escola 3ESR — API REST (Checkpoint 5)

**Disciplina:** SOA e WebServices · **Professor:** Carlos Eduardo Machado de Oliveira

## Integrantes

| Nome | RM |
|---|---|
| Alexia Ramalho | 558385 |
| Enzo Real | 557943 |
| Gustavo Pasquini | 555454 |
| Hellen Silva | 559008 |
| Lorenzo Acquesta | 557397 |

---

## 1. Descrição

API REST em Spring Boot para o agendamento de instruções de uma autoescola. Inclui:

- CRUD de **Instrutores**, **Alunos** e **Usuários**;
- **Instruções**: agendamento, listagem, detalhe e cancelamento, com regras de negócio (RN-A e RN-C);
- consumo do WebService externo **ViaCEP** (`GET /enderecos/{cep}`);
- autenticação **JWT** stateless com perfis `ADMIN` e `USER`;
- documentação **Swagger/OpenAPI**, **CORS** configurável por propriedade e testes automatizados.

**Stack:** Java 21 · Spring Boot 4.0.5 · Spring Security · Spring Data JPA · Flyway · MySQL 8 · springdoc-openapi · JUnit 5 / Mockito / H2 (testes).

## 2. Arquitetura

Organização inspirada em *ports & adapters* (pacote raiz `br.com.fiap3esr.autoescola3esr`):

| Pacote | Conteúdo |
|---|---|
| `adapter/in/controller` | Controllers REST e DTOs (`request`/`response`) |
| `adapter/out/client/viacep` | Cliente do ViaCEP (`ViaCepClient`, implementa a porta `ConsultaCepGateway`) |
| `adapter/out/security` | `AutenticacaoService` |
| `application/core/domain` | Entidades `Aluno`, `Instrutor`, `Usuario`, `Instrucao` |
| `application/core/specification` | Validadores das regras de negócio (agendamento e cancelamento) |
| `application/port/out` | Portas: repositórios e `ConsultaCepGateway` |
| `application/service` | Casos de uso (`AlunoService`, `InstrutorService`, `UsuarioService`, `AgendaDeInstrucoes`, `EnderecoService`) |
| `config` | Segurança (JWT, CORS) e Swagger |
| `exception` | `GlobalExceptionHandler` e exceções de domínio |

**ADR-01 (resumo):** as classes de domínio são as próprias entidades JPA, e as portas de persistência são interfaces que estendem `JpaRepository`. A separação "pura" (entidade de persistência + mapper) foi descartada por custar mais sem acrescentar valor à entrega; o adaptador de persistência incompleto de `Instrutor` foi removido.

**Decisão sobre instruções:** não há atualização (PUT) de instrução. Para mudar uma aula, cancela-se e agenda-se outra, porque um reagendamento precisaria repetir todas as validações.

## 3. Pré-requisitos

- **JDK 21**
- **MySQL 8** em `localhost:3306`, usuário `root`, senha `fiap`
- Banco criado:

```sql
create database autoescola3esr;
```

As tabelas são criadas pelo **Flyway** (`src/main/resources/db/migration`, V1 a V10).

> **Atenção:** se o banco `autoescola3esr` já tiver sido usado por outra versão do projeto com migrations V9/V10 diferentes, o Flyway acusa *checksum mismatch* na subida. Nesse caso use um banco novo (`drop database autoescola3esr; create database autoescola3esr;`) ou execute `flyway repair` conscientemente.

Variável de ambiente opcional: `JWT_SECRET` (segredo do token; padrão `12345678` apenas para desenvolvimento).

## 4. Como rodar

```bash
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

A API sobe na porta **8081**: `http://localhost:8081`.

## 5. Como testar

```bash
./mvnw test                   # testes
./mvnw clean verify           # build completo (critério de pronto)
```

Os testes **não precisam de MySQL**: usam H2 em memória (modo MySQL) no profile `test` (`src/test/resources/application-test.properties`) e **nunca acessam a internet** (o ViaCEP é simulado).

## 6. Credenciais, Swagger e coleção de requisições

- **Usuário administrador** (criado pela migration `V10__insert-usuario-admin.sql`): login `admin`, senha `admin`.
- **Swagger UI:** <http://localhost:8081/swagger-ui.html> · JSON: `/v3/api-docs` · YAML: `/v3/api-docs.yaml`.
- **Usando o token no Swagger:**
  1. execute `POST /login` com `{"login":"admin","senha":"admin"}`;
  2. copie o valor de `tokenJWT` da resposta;
  3. clique em **Authorize** (esquema `bearer-key`), cole o token (sem o prefixo `Bearer`) e confirme.
- **Coleção de requisições:** [`docs/requests.http`](docs/requests.http) (REST Client do VS Code ou HTTP Client do IntelliJ), começando pelo login e usando a variável `@token`.
- **Swagger offline:** [`SwaggerOffLine.yaml`](SwaggerOffLine.yaml). Foi gerado com a aplicação real em execução (MySQL 8, Flyway V1–V10 aplicadas), salvando a resposta de `GET http://localhost:8081/v3/api-docs.yaml`.

## 7. Endpoints

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/login` | público | Autentica e devolve o token JWT |
| GET | `/health-check` | público | Verificação de disponibilidade |
| POST | `/instrutores` | ADMIN | Cadastra instrutor (201 + `Location`) |
| GET | `/instrutores` | ADMIN, USER | Lista instrutores ativos (paginado) |
| GET | `/instrutores/{id}` | ADMIN | Detalha instrutor |
| PUT | `/instrutores` | ADMIN | Atualiza instrutor |
| DELETE | `/instrutores/{id}` | ADMIN | Exclusão lógica (204) |
| POST | `/alunos` | ADMIN | Cadastra aluno (201 + `Location`) |
| GET | `/alunos` | ADMIN, USER | Lista alunos ativos (paginado) |
| GET | `/alunos/{id}` | ADMIN | Detalha aluno |
| PUT | `/alunos` | ADMIN | Atualiza aluno (CPF não é alterável) |
| DELETE | `/alunos/{id}` | ADMIN | Exclusão lógica (204) |
| POST | `/usuarios` | ADMIN | Cria usuário (senha em BCrypt; perfil padrão `USER`) |
| GET | `/usuarios` | ADMIN | Lista usuários (ordenado por `login`) |
| GET | `/usuarios/{id}` | ADMIN | Detalha usuário |
| PUT | `/usuarios` | ADMIN | Atualiza senha e/ou perfil |
| DELETE | `/usuarios/{id}` | ADMIN | Exclusão física (não pode excluir a si mesmo) |
| POST | `/instrucoes` | ADMIN, USER | Agenda instrução |
| GET | `/instrucoes` | ADMIN, USER | Lista instruções (ordem `dataHora`); `?ativas=true` traz só as não canceladas |
| GET | `/instrucoes/{id}` | ADMIN, USER | Detalha instrução, com status e motivo de cancelamento |
| DELETE | `/instrucoes` | ADMIN, USER | Cancela instrução (corpo `{"id_instrucao", "motivo"}`, 204) |
| GET | `/enderecos/{cep}` | ADMIN, USER | Consulta endereço no ViaCEP |

A senha nunca é retornada. Erros seguem o formato `{"mensagem": "..."}` (400 regra de negócio, 401, 403, 404, 503) ou uma lista `{"campo","mensagem"}` para erros de validação (400).

## 8. Regras de negócio

### Agendamento (RN-A)
| ID | Regra | Implementação |
|---|---|---|
| RN-A1 | Segunda a sábado, das 06:00 às 21:00 (última aula começa às 20:00) | `ValidadorHorarioFuncionamento` |
| RN-A2 | Antecedência mínima de 30 minutos | `ValidadorHorarioAntecedencia` |
| RN-A3 | Aula começa em hora inteira | `ValidadorHoraInteira` |
| RN-A4 | Aluno inativo não pode agendar | `ValidadorAlunoAtivo` |
| RN-A5 | Instrutor inativo não pode ser agendado | `ValidadorInstrutorAtivo` |
| RN-A6 | Instrutor sem outra instrução **não cancelada** no mesmo horário | `ValidadorConflitoHorarioInstrutor` |
| RN-A7 | Aluno com no máximo 1 instrução **não cancelada** por dia | `ValidadorLimiteDiarioAluno` |
| RN-A8 | Sem `id_instrutor`, `especialidade` é obrigatória e um instrutor ativo e livre é escolhido aleatoriamente | `AgendaDeInstrucoes` + `InstrutorRepository.escolherInstrutorAleatorioDisponivel` |

### Cancelamento (RN-C)
| ID | Regra | Resposta |
|---|---|---|
| RN-C1 | `motivo` obrigatório (`ALUNO_DESISTIU`, `INSTRUTOR_CANCELOU`, `OUTROS`) | 400 (validação) |
| RN-C2 | A instrução precisa existir | 404 `"ID da instrução informado não existe!"` |
| RN-C3 | Antecedência mínima de 24 horas | 400 `"Instrução somente pode ser cancelada com antecedência mínima de 24 horas!"` |
| RN-C4 | Não cancelar instrução já cancelada | 400 `"Instrução já está cancelada!"` |

Uma instrução está cancelada quando `motivo_cancelamento` (migration V9) não é nulo.

## 9. Integração ViaCEP

- Porta `ConsultaCepGateway` → adapter `ViaCepClient` (Spring `RestClient`), URL base em `api.integracao.viacep.url=https://viacep.com.br/ws`.
- Timeouts: conexão 3 s, leitura 5 s.
- Respostas: CEP fora do formato `\d{5}-?\d{3}` → 400 `"CEP inválido!"`; `{"erro": true}` → 404 `"CEP não encontrado!"`; ViaCEP fora do ar/timeout/5xx → 503 `"Serviço de CEP indisponível no momento!"`.
- **Testes:** `ViaCepClientTest` usa `MockRestServiceServer.bindTo(RestClient.builder())` em vez de `@RestClientTest`. É um desvio aceito da especificação: a alternativa sem *slice* é mais simples no Spring Boot 4 (onde o `@RestClientTest` mudou de módulo) e mantém a garantia principal — nenhuma chamada real à internet.

## 10. CORS

Configurado em `config/security/CorsConfig.java` (bean `CorsConfigurationSource`, ativado por `http.cors(Customizer.withDefaults())`). Não há origens fixas no código: a lista vem da propriedade

```properties
api.cors.allowed-origins=http://localhost:3000,http://127.0.0.1:3000,http://localhost:5500,http://127.0.0.1:5500,http://localhost:5173
```

**Para liberar uma nova origem**, acrescente-a à lista (separada por vírgula, sem barra no final) em `src/main/resources/application.properties` — ou sobrescreva sem editar o arquivo, por exemplo com a variável de ambiente `API_CORS_ALLOWED_ORIGINS` ou `--api.cors.allowed-origins=...` na linha de comando — e reinicie a aplicação.

Métodos: GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD · Headers: Authorization, Content-Type, Accept, Origin · Exposto: `Location` · `allowCredentials=true` · `maxAge=3600`.

## 11. Mapeamento dos requisitos do enunciado

| # | Requisito | Onde está |
|---|---|---|
| E1 | CRUD de Instrutores, Alunos, Usuários e Instruções | `InstrutorController`, `AlunoController`, `UsuarioController`, `InstrucaoController` + services em `application/service` |
| E2 | Agendamento e cancelamento com regras de negócio | `AgendaDeInstrucoes`, `application/core/specification/**`, migration V9 |
| E3 | Consumo de API externa | `ViaCepClient`, `EnderecoService`, `EnderecoController` |
| E4 | Swagger | `SwaggerConfig`, anotações `@Tag`/`@Operation`/`@ApiResponse`, `SwaggerOffLine.yaml`, `OpenApiDocsTest` |
| E5 | CORS | `CorsConfig`, `SecurityConfig`, `CorsConfigTest` |
| E6 | Testes por entidade | `src/test/java/**`: `InstrutorControllerTest`, `InstrutorRepositoryTest`, `AlunoControllerTest`, `AlunoRepositoryTest`, `UsuarioControllerTest`, `UsuarioServiceTest`, `UsuarioRepositoryTest`, `InstrucaoControllerTest`, `InstrucaoRepositoryTest`, `AgendaDeInstrucoesTest`, `ValidadoresAgendamentoTest`, `CancelamentoInstrucaoTest`, `ViaCepClientTest`, `EnderecoControllerTest`, `CorsConfigTest`, `OpenApiDocsTest` |
| E7 | README com integrantes | este arquivo (seção Integrantes) |

## 12. Documentação adicional

- Especificação SDD original: [`docs/SPEC.md`](docs/SPEC.md)
- Enunciado: `docs/Checkpoint 5 - Enunciado -.docx`
