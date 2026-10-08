# Auto Escola 3ESR — API REST (Checkpoint 5)

> **Este README é uma especificação executável (Spec-Driven Development).**
> Ele foi escrito para ser lido e executado por um agente de código.
> Siga as tarefas da seção 7 **na ordem**, cumprindo os critérios de aceite de cada uma
> antes de avançar. Ao final, reescreva este README conforme a tarefa T12.

**Disciplina:** SOA e WebServices · **Professor:** Carlos Eduardo Machado de Oliveira
**Entrega:** repositório público no GitHub, até **12/10/2026**

**Integrantes:**
- Alexia Ramalho — RM 558385
- Enzo Real — RM 557943
- Gustavo Pasquini — RM 555454
- Hellen Silva — RM 559008
- Lorenzo Acquesta — RM 557397

---

## 1. Objetivo

Entregar uma API REST em Spring Boot para o agendamento de instruções de uma autoescola. A API deve conter tudo o que foi pedido nos checkpoints anteriores e também o que o Checkpoint 5 pede:

| # | Requisito do enunciado | Seção da spec |
|---|---|---|
| E1 | CRUD completo de Instrutores, Alunos, Usuários e Instruções | RF-01 a RF-04 |
| E2 | Agendamento e cancelamento de instruções com as regras de negócio | RF-04, RN-* |
| E3 | Consumo de uma API ou WebService externo | RF-05 |
| E4 | Documentação automática com Swagger | RF-06 |
| E5 | Configuração de CORS | RF-07 |
| E6 | Testes automatizados para cada entidade | RF-08 |
| E7 | README com os nomes dos integrantes | T12 |

## 2. Restrições técnicas

- **Java:** a versão mínima pedida é a 17. A máquina de desenvolvimento tem o **JDK 21** (Temurin 21.0.12), e o `pom.xml` declara `25`, que **não compila** aqui. Use `21`.
- **Framework:** Spring Boot **4.0.5**. Não troque de versão.
- **Pacote raiz:** `br.com.fiap3esr.autoescola3esr`. Não renomeie.
- **Build:** use o Maven Wrapper (`./mvnw` ou `mvnw.cmd`). O comando que precisa passar é `./mvnw clean verify`.
- **Banco:** MySQL em `localhost:3306/autoescola3esr` (usuário `root`, senha `fiap`), com migrations no Flyway em `src/main/resources/db/migration`.
- **Migrations:** nunca altere uma migration existente (V1 a V8). Para mudar o schema, crie `V9__...`, `V10__...` e assim por diante.
- **Testes:** precisam rodar **sem um MySQL instalado**. Use H2 em modo MySQL no profile `test` (ver T1).
- **Idioma:** código, mensagens de erro e documentação em **português**, seguindo o estilo atual (records `DadosXxx`, mensagens como `"ID do aluno informado não existe!"`).
- **Segurança:** autenticação stateless com JWT (`/login`). Perfis: `ADMIN` e `USER`.
- **Encoding:** os arquivos estão em UTF-8 e devem continuar assim.

## 3. Estado atual do código (diagnóstico)

O projeto veio de `AutoEscola3ESR 12/AutoEscola3ESR` e foi copiado como base. A migração para arquitetura hexagonal estava **pela metade**, por isso o projeto **não compila** hoje.

### 3.1 O que já funciona (ou funcionará depois do T1)
- Login com JWT: `LoginController`, `TokenService`, `SecurityFilter` e `AutenticacaoService`.
- CRUD de Instrutor: `InstrutorController` e `InstrutorService`, com exclusão lógica via `ativo`.
- `POST /instrucoes` (agendamento) em `AgendaDeInstrucoes`, com 7 validadores em `application/core/specification`.
- Swagger via springdoc 3.1.0, com `SwaggerConfig` e o esquema `bearer-key`.
- CORS: está inline em `SecurityConfig`.
- Migrations V1 a V8: tabelas `instrutores`, `usuarios`, `alunos` e `instrucoes`.

### 3.2 Defeitos conhecidos (corrigidos no T1)
| ID | Arquivo | Problema |
|---|---|---|
| D1 | `pom.xml` | `<java.version>25</java.version>`, mas o JDK disponível é o 21. |
| D2 | `application/service/AgendaDeInstrucoes.java` | `import br.com.fiap3esr.autoescola3esr.domain.agenda.*;` aponta para um pacote que não existe. |
| D3 | `application/core/domain/Instrutor.java` | É um POJO sem `@Entity`, mas `Instrucao` o referencia com `@ManyToOne`. O mapeamento JPA quebra. |
| D4 | `application/port/out/InstrutorRepository.java` | Não declara `save`, `findById`, `existsById` nem `getReferenceById`, que são usados por `InstrutorService` e `AgendaDeInstrucoes`. |
| D5 | `adapter/out/repository/InstrutorRepositoryImpl.java` | Todos os métodos retornam `null` ou `false` (stub). |
| D6 | `test/.../InstrutorRepositoryTest.java` | Persiste `Instrutor` com `TestEntityManager`, o que exige que ele seja `@Entity`. |
| D7 | `application/core/domain/Instrucao.java` | O campo `dataHora` não tem modificador `private`. |
| D8 | `config/security/filter/SecurityFilter.java` | Um token inválido lança `RuntimeException`, que vira 500. O correto é 401/403. |
| D9 | `exception/GlobalExceptionHandler.java` | Não trata `ValidacaoException`, `AlunoNotFoundException` nem `InstrutorNotFoundException`, então tudo vira 500. |

### 3.3 Decisão de arquitetura (ADR-01)

**Decisão:** padronizar todas as entidades no mesmo modelo que `Aluno`, `Usuario` e `Instrucao` já usam:
- a classe de domínio em `application/core/domain` tem as anotações JPA;
- a porta em `application/port/out` é uma interface que estende `JpaRepository`.

**Consequência:** `Instrutor` volta a ser `@Entity` (tabela `instrutores`). Os arquivos abaixo devem ser **removidos**:
- `adapter/out/repository/entity/InstrutorEntity.java`
- `adapter/out/repository/persistence/InstrutorJpaRepository.java`
- `adapter/out/repository/InstrutorRepositoryImpl.java`

A `@Query` de `escolherInstrutorAleatorioDisponivel` vai para `InstrutorRepository`.

**Motivo:** é o menor caminho até um build verde, deixa as quatro entidades consistentes e mantém o teste `InstrutorRepositoryTest` válido. Para a entrega, uma arquitetura hexagonal "pura", com entidade de persistência separada e mapper, custa mais e não acrescenta nada. Não reintroduza `InstrutorEntity`.

**Pastas que continuam valendo:**
- `adapter/in/controller`: controllers e DTOs de request e response
- `adapter/out/...`: integrações externas, como o cliente ViaCEP e a segurança
- `application/service`: casos de uso
- `application/core/specification`: regras de negócio
- `application/port/out`: portas

## 4. Requisitos funcionais

Convenções que valem para **todos** os CRUDs:
- **Listagem:** paginada com `@PageableDefault(size = 10, sort = "nome")` e `@ParameterObject`. Retorna `Page<DadosListagemXxx>`.
- **Cadastro:** responde `201 Created` com o header `Location` e o corpo `DadosDetalhamentoXxx`.
- **Atualização:** `PUT /recurso` com o `id` no corpo, como já faz `InstrutorController`. Responde `200` com `DadosDetalhamentoXxx`.
- **Exclusão:** responde `204 No Content`.
- **ID inexistente:** responde `404`, com corpo `{ "mensagem": "..." }`.
- **Erro de validação:** responde `400`, com uma lista de `{ "campo", "mensagem" }` (formato já existente).
- **Regra de negócio violada** (`ValidacaoException`): responde `400` com `{ "mensagem": "..." }`.
- **Controllers protegidos:** todos têm `@SecurityRequirement(name = "bearer-key")` e `@Tag`.

### RF-01 — Instrutores (`/instrutores`) — já existe, só ajustar
| Método | Rota | Perfil | Comportamento |
|---|---|---|---|
| POST | `/instrutores` | ADMIN | Cadastra o instrutor com `ativo = true`. |
| GET | `/instrutores` | ADMIN, USER | Lista apenas os instrutores ativos. |
| GET | `/instrutores/{id}` | ADMIN | Detalha o instrutor. |
| PUT | `/instrutores` | ADMIN | Atualiza nome, email, telefone, especialidade e endereço (parcial). |
| DELETE | `/instrutores/{id}` | ADMIN | Exclusão lógica (`ativo = false`). |

### RF-02 — Alunos (`/alunos`) — **criar**
Espelhe exatamente o RF-01, trocando `cnh`/`especialidade` por `cpf`.
- **DTOs:** `DadosCadastroAluno`, `DadosAtualizacaoAluno`, `DadosListagemAluno` e `DadosDetalhamentoAluno`, no pacote `adapter/in/controller/{request,response}/aluno`.
- **Validação do cadastro:**
  - `nome`: `@NotBlank`
  - `email`: `@NotBlank @Email`
  - `telefone`: `@NotBlank`
  - `cpf`: `@NotBlank @Pattern("\\d{11}")`
  - `endereco`: `@NotNull @Valid DadosEndereco`
- **Atualização:** não permite alterar `cpf`.
- **Domínio:** `Aluno` ganha o construtor `Aluno(DadosCadastroAluno)` e os métodos `atualizarInformacoes(...)` e `excluir()`, no mesmo padrão de `Instrutor`.
- **Serviço:** `application/service/AlunoService`.
- **Porta:** `AlunoRepository` ganha `Page<Aluno> findAllByAtivoTrue(Pageable)`.
- **Perfis:** os mesmos do RF-01.

### RF-03 — Usuários (`/usuarios`) — **criar**
| Método | Rota | Perfil | Comportamento |
|---|---|---|---|
| POST | `/usuarios` | ADMIN | Cria o usuário. A senha é gravada com `PasswordEncoder` (BCrypt). Se `perfil` não vier, usa `USER`. |
| GET | `/usuarios` | ADMIN | Lista os usuários, ordenando por `login`. |
| GET | `/usuarios/{id}` | ADMIN | Detalha o usuário. |
| PUT | `/usuarios` | ADMIN | Atualiza `senha` (gravada como hash) e/ou `perfil`. |
| DELETE | `/usuarios/{id}` | ADMIN | Exclusão física. Não pode excluir o próprio usuário logado (responde `400`). |

- **Senha:** nunca é retornada em nenhuma resposta.
- **Login duplicado:** responde `400` com a mensagem `"Login já cadastrado!"`.
- **Validação:**
  - `login`: `@NotBlank`
  - `senha`: `@NotBlank @Size(min = 6)` no cadastro
- **Domínio:** `Usuario` ganha um construtor e o método `atualizar(senhaHash, perfil)`.

### RF-04 — Instruções (`/instrucoes`) — completar
| Método | Rota | Perfil | Comportamento |
|---|---|---|---|
| POST | `/instrucoes` | ADMIN, USER | Agenda a instrução (já existe). Deve aplicar todas as RN-A. |
| GET | `/instrucoes` | ADMIN, USER | Lista paginada, ordenando por `dataHora`. Aceita o filtro opcional `?ativas=true`, que traz só as não canceladas. |
| GET | `/instrucoes/{id}` | ADMIN, USER | Detalha a instrução, incluindo o status e o motivo de cancelamento. |
| DELETE | `/instrucoes` | ADMIN, USER | Cancela a instrução. O corpo `DadosCancelamento` tem `id_instrucao` e `motivo`. Deve aplicar as RN-C e responde `204`. |

A atualização de uma instrução **não** será exposta. A regra é cancelar e agendar de novo, porque um reagendamento precisaria repetir todas as validações. Cite essa decisão no README final.

**Persistência:** criar a migration `V9__alter-table-instrucoes-add-column-motivo-cancelamento.sql` com:
```sql
alter table instrucoes add column motivo_cancelamento varchar(30);
```
- **Enum:** `MotivoCancelamento { ALUNO_DESISTIU, INSTRUTOR_CANCELOU, OUTROS }`, em `shared/vo/enumeration`.
- **Domínio:** `Instrucao` ganha o campo `@Enumerated(STRING) motivoCancelamento` e o método `cancelar(MotivoCancelamento)`.
- **Status:** uma instrução está cancelada quando `motivoCancelamento != null`.

#### Regras de negócio do agendamento (RN-A), já implementadas como validadores
| ID | Regra | Validador |
|---|---|---|
| RN-A1 | Funcionamento de segunda a sábado, das 06:00 às 21:00. A última aula começa às 20:00. | `ValidadorHorarioFuncionamento` |
| RN-A2 | O agendamento precisa ser feito com pelo menos 30 minutos de antecedência. | `ValidadorHorarioAntecedencia` |
| RN-A3 | A aula começa em hora inteira. | `ValidadorHoraInteira` |
| RN-A4 | Não é permitido agendar para aluno inativo. | `ValidadorAlunoAtivo` |
| RN-A5 | Não é permitido agendar com instrutor inativo. | `ValidadorInstrutorAtivo` |
| RN-A6 | O instrutor não pode ter outra instrução **não cancelada** no mesmo horário. | `ValidadorConflitoHorarioInstrutor` |
| RN-A7 | O aluno pode ter no máximo 1 instrução **não cancelada** por dia. | `ValidadorLimiteDiarioAluno` |
| RN-A8 | Se `id_instrutor` não for informado, `especialidade` passa a ser obrigatória, e o sistema escolhe aleatoriamente um instrutor ativo e livre. | `AgendaDeInstrucoes` |

> Ajuste obrigatório: as consultas usadas em RN-A6, RN-A7 e RN-A8 precisam **ignorar instruções canceladas**, ou seja, filtrar por `motivoCancelamento is null`.
> - RN-A6: `existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull`
> - RN-A7: `existsByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull`
> - RN-A8: na subquery de `escolherInstrutorAleatorioDisponivel`, acrescente `and a.motivoCancelamento is null`.

#### Regras de negócio do cancelamento (RN-C), **criar**
Os validadores ficam numa interface nova, `ValidadorCancelamento`, em `application/core/specification/contract`. Use o mesmo padrão de `List<ValidadorCancelamento>` injetada no serviço.
| ID | Regra | Mensagem |
|---|---|---|
| RN-C1 | O `motivo` é obrigatório (`@NotNull` no DTO). | validação padrão, `400` |
| RN-C2 | A instrução precisa existir. | `"ID da instrução informado não existe!"`, `404` |
| RN-C3 | O cancelamento precisa ser feito com pelo menos **24 horas** de antecedência. | `"Instrução somente pode ser cancelada com antecedência mínima de 24 horas!"` |
| RN-C4 | Não é permitido cancelar uma instrução já cancelada. | `"Instrução já está cancelada!"` |

> Premissa: o enunciado menciona um "documento anexo" com as regras, mas ele não estava disponível. As regras RN-C seguem o padrão do curso. Se o anexo divergir, ajuste só esta tabela e os validadores.

### RF-05 — Consumo de API externa: ViaCEP — **criar**
**Objetivo:** consultar endereços por CEP no WebService público `https://viacep.com.br/ws/{cep}/json/`.

**Design (ports & adapters):**
- **Porta:** `application/port/out/ConsultaCepGateway`, com o método `DadosEndereco consultar(String cep)`.
- **Adapter:** `adapter/out/client/viacep/ViaCepClient implements ConsultaCepGateway`.
  - Usa o `RestClient` do Spring, criado a partir de um `RestClient.Builder` injetado.
  - A URL base vem da propriedade `api.integracao.viacep.url=https://viacep.com.br/ws`, em `application.properties`.
  - O record interno `ViaCepResponse(cep, logradouro, complemento, bairro, localidade, uf, erro)` é mapeado para `DadosEndereco`, onde `localidade` vira `cidade` e `numero` fica `null`.
- **Serviço:** `application/service/EnderecoService`.
- **Controller:** `adapter/in/controller/EnderecoController`, com `GET /enderecos/{cep}` para os perfis ADMIN e USER.

**Respostas:**
| Situação | Resposta |
|---|---|
| CEP fora do formato `\d{5}-?\d{3}` | `400`, `"CEP inválido!"` |
| ViaCEP responde `{"erro": true}` | `404`, `"CEP não encontrado!"` |
| ViaCEP fora do ar, com timeout ou status 5xx | `503`, `"Serviço de CEP indisponível no momento!"`. Use a exceção `IntegracaoExternaException`. |

- **Timeouts:** conexão de 3 s e leitura de 5 s.
- **Uso no cadastro:** opcional e desejável. Em `POST /alunos` e `POST /instrutores`, quando o endereço vier só com `cep` e `numero`, os demais campos são completados pelo ViaCEP. Se for implementar, mova as validações de endereço para depois do preenchimento. **Se ficar complexo, não implemente.** O endpoint `GET /enderecos/{cep}` sozinho já atende o requisito E3.

### RF-06 — Swagger / OpenAPI
- **Endpoints públicos:** a UI fica em `/swagger-ui.html` e o JSON em `/v3/api-docs`. Os dois já estão liberados no `SecurityConfig`.
- **`SwaggerConfig`:** manter o esquema `bearer-key` e acrescentar `version("1.0")` e a lista de integrantes em `description`.
- **Controllers:** todos recebem `@Tag(name, description)`. Todo endpoint recebe `@Operation(summary)` e `@ApiResponse` para os principais status (201/200/204/400/401/403/404).
- **Segurança na documentação:** `InstrucaoController`, `AlunoController`, `UsuarioController` e `EnderecoController` recebem `@SecurityRequirement(name = "bearer-key")`.
- **Arquivo offline:** regenerar `SwaggerOffLine.yaml` a partir de `/v3/api-docs.yaml` com a aplicação rodando. Se não houver MySQL disponível, pule esse passo e registre isso no README.

### RF-07 — CORS
- **Configuração:** extrair o CORS de `SecurityConfig` para um bean `CorsConfigurationSource`, em `config/security/CorsConfig.java`, e usar `http.cors(Customizer.withDefaults())`.
- **Origens:** lidas de uma propriedade, sem valores fixos no código:
  `api.cors.allowed-origins=http://localhost:3000,http://127.0.0.1:3000,http://localhost:5500,http://127.0.0.1:5500,http://localhost:5173`
- **Métodos:** GET, POST, PUT, PATCH, DELETE, OPTIONS e HEAD.
- **Headers permitidos:** Authorization, Content-Type, Accept e Origin.
- **Header exposto:** `Location`.
- **Outros parâmetros:** `allowCredentials=true` e `maxAge=3600`.
- **Rota pública:** liberar também `/health-check` com `permitAll()`.

### RF-08 — Testes automatizados
Os testes rodam no profile `test` com H2 (ver T1). Os nomes seguem o padrão `metodoCenarioN`, com `@DisplayName("Expectativa: ...")`, como nos testes que já existem. Cada entidade precisa de **pelo menos**:

| Entidade | Teste | Tipo | Cenários mínimos |
|---|---|---|---|
| Instrutor | `InstrutorControllerTest` | `@SpringBootTest` + MockMvc + `@MockitoBean InstrutorService` | Cadastro válido como ADMIN → 201. Corpo inválido → 400. Cadastro como USER → 403. Sem token → 401/403. Detalhar ID inexistente → 404. |
| Instrutor | `InstrutorRepositoryTest` (já existe) | `@DataJpaTest` | Instrutor ocupado → null. Livre → retorna o instrutor. Acrescentar: com instrução cancelada no horário → retorna o instrutor. |
| Aluno | `AlunoControllerTest` | MockMvc | Cadastro → 201. Inválido (cpf com 10 dígitos) → 400. Listar → 200. Excluir → 204. ID inexistente → 404. |
| Aluno | `AlunoRepositoryTest` | `@DataJpaTest` | `findAllByAtivoTrue` ignora inativos. `existsByIdAndAtivoFalse`. |
| Usuário | `UsuarioControllerTest` | MockMvc | ADMIN cria → 201 e a resposta não contém `senha`. USER tenta criar → 403. Login duplicado → 400. |
| Usuário | `UsuarioServiceTest` | Unitário (Mockito) | A senha é gravada como hash (verificar `passwordEncoder.encode`). O perfil padrão é USER. |
| Instrução | `InstrucaoControllerTest` (já existe, ampliar) | MockMvc | Agendar inválido → 400 (já existe). Agendar válido → 200. Cancelar sem motivo → 400. Cancelar válido → 204. |
| Instrução | `AgendaDeInstrucoesTest` | Unitário (Mockito) | Aluno inexistente → `AlunoNotFoundException`. Sem instrutor e sem especialidade → `ValidacaoException`. Nenhum instrutor livre → `ValidacaoException`. Sucesso → `save` chamado. |
| Instrução | `ValidadoresAgendamentoTest` | Unitário puro | Cada RN-A1, A2 e A3 com um caso válido e um inválido. |
| Instrução | `CancelamentoInstrucaoTest` | Unitário | RN-C3 (menos de 24 h → exceção) e RN-C4. |
| ViaCEP | `ViaCepClientTest` | `@RestClientTest(ViaCepClient.class)` + `MockRestServiceServer` | CEP válido → `DadosEndereco` mapeado. `{"erro":true}` → exceção de não encontrado. 500 → `IntegracaoExternaException`. **Nunca chamar a API real nos testes.** |
| CORS | `CorsConfigTest` | MockMvc | `OPTIONS /instrutores` com `Origin: http://localhost:3000` → header `Access-Control-Allow-Origin` presente. Com uma origem não listada → 403. |

`AutoEscola3EsrApplicationTests.contextLoads` precisa passar com `@ActiveProfiles("test")`.

## 5. Requisitos não funcionais
- **RNF-01:** `./mvnw clean verify` termina com **BUILD SUCCESS** e com 0 testes falhando, sem MySQL.
- **RNF-02:** nenhum endpoint retorna 500 para erro de cliente. Todas as exceções de domínio passam pelo `GlobalExceptionHandler`.
- **RNF-03:** nenhuma senha ou segredo novo fica hardcoded. O `api.security.token.secret` passa a aceitar a variável de ambiente `${JWT_SECRET:12345678}`.
- **RNF-04:** nenhum warning novo de compilação causado por código adicionado.

## 6. Mapa de arquivos esperado (resultado final)

```
src/main/java/br/com/fiap3esr/autoescola3esr/
├── adapter/
│   ├── in/controller/
│   │   ├── AlunoController.java            (novo)
│   │   ├── EnderecoController.java         (novo)
│   │   ├── InstrucaoController.java        (ampliar)
│   │   ├── InstrutorController.java
│   │   ├── UsuarioController.java          (novo)
│   │   ├── HealthCheckController.java
│   │   ├── LoginController.java
│   │   ├── request/{aluno,instrucao,instrutor,usuario}/...
│   │   └── response/{aluno,instrucao,instrutor,usuario}/...
│   └── out/
│       ├── client/viacep/{ViaCepClient,ViaCepResponse}.java   (novo)
│       └── security/AutenticacaoService.java
│       (REMOVER: repository/entity, repository/persistence, InstrutorRepositoryImpl)
├── application/
│   ├── core/domain/{Aluno,Instrucao,Instrutor,Usuario}.java
│   ├── core/specification/
│   │   ├── contract/{ValidadorAgendamento,ValidadorCancelamento}.java
│   │   ├── Validador*.java  (agendamento, já existem)
│   │   └── cancelamento/{ValidadorAntecedenciaCancelamento,ValidadorInstrucaoJaCancelada}.java (novo)
│   ├── port/out/{AlunoRepository,InstrucaoRepository,InstrutorRepository,UsuarioRepository,ConsultaCepGateway}.java
│   └── service/{AgendaDeInstrucoes,AlunoService,EnderecoService,InstrutorService,UsuarioService}.java
├── config/
│   ├── documentation/SwaggerConfig.java
│   └── security/{SecurityConfig,CorsConfig}.java, filter/, service/
├── exception/GlobalExceptionHandler.java
│   └── type/{AlunoNotFoundException,InstrutorNotFoundException,InstrucaoNotFoundException,
│             UsuarioNotFoundException,CepNotFoundException,IntegracaoExternaException,ValidacaoException}.java
└── shared/vo/{endereco, enumeration/{Especialidade,Role,MotivoCancelamento}}
src/main/resources/db/migration/V9__alter-table-instrucoes-add-column-motivo-cancelamento.sql (novo)
src/test/resources/application-test.properties (novo, H2)
```

## 7. Plano de execução (tarefas)

Faça uma tarefa de cada vez. Ao terminar cada uma, rode o comando de verificação indicado. **Não avance com o build quebrado.**

### T0 — Higiene do repositório
- Apagar a pasta `target/`, que é um artefato copiado. A `.idea/` já está no `.gitignore`.
- Rodar `git init`, caso ainda não seja um repositório git.
- Mover `Checkpoint 5 - Enunciado -.docx` para `docs/`.
- **Verificação:** `git status` não lista `target/`.

### T1 — Build verde (corrige D1 a D9)
1. No `pom.xml`, trocar `<java.version>` para `21`.
2. No `pom.xml`, adicionar `com.h2database:h2` com `<scope>test</scope>`.
3. Criar `src/test/resources/application-test.properties`:
   ```properties
   spring.datasource.url=jdbc:h2:mem:autoescola3esr_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
   spring.datasource.username=sa
   spring.datasource.password=
   spring.flyway.enabled=true
   api.security.token.secret=test-secret
   ```
   Remover `src/main/resources/application-test.properties`, porque o de teste o substitui.
4. Aplicar a ADR-01:
   - transformar `Instrutor` em `@Entity(name = "Instrutor") @Table(name = "instrutores")`, com `@Id @GeneratedValue(IDENTITY)`, `@Enumerated(STRING)` em especialidade e `@Embedded` em endereço;
   - transformar `InstrutorRepository` em `extends JpaRepository<Instrutor, Long>`, com a `@Query` e os métodos derivados;
   - apagar os três arquivos do adapter de persistência.
5. Remover o import inválido de `AgendaDeInstrucoes` (D2).
6. Tornar `dataHora` `private` em `Instrucao` (D7).
7. No `SecurityFilter`, tratar um token inválido devolvendo `401` em vez de deixar propagar 500 (D8).
8. No `GlobalExceptionHandler`, mapear:
   - `ValidacaoException` → 400
   - `*NotFoundException` → 404
   - `BadCredentialsException` → 401
   - `AccessDeniedException` → 403
   - `IntegracaoExternaException` → 503
   - `Exception` genérica → 500 com mensagem neutra

   Todos respondem com o corpo `{ "mensagem": ... }` (D9).
9. Nos testes existentes, acrescentar `@ActiveProfiles("test")` onde não houver.
- **Verificação:** `./mvnw clean verify` → BUILD SUCCESS, com os 3 testes atuais passando.

### T2 — CRUD de Alunos (RF-02)
- **Verificação:** build verde, mais os testes do RF-08 para Aluno.

### T3 — CRUD de Usuários (RF-03)
- **Verificação:** build verde, mais os testes do RF-08 para Usuário.

### T4 — Instruções: listagem, detalhe e cancelamento (RF-04, RN-C, migration V9, ajustes das RN-A)
- **Verificação:** build verde, mais os testes do RF-08 para Instrução e o novo cenário em `InstrutorRepositoryTest`.

### T5 — Integração ViaCEP (RF-05)
- **Verificação:** build verde, mais `ViaCepClientTest`.

### T6 — CORS (RF-07)
- **Verificação:** build verde, mais `CorsConfigTest`.

### T7 — Swagger (RF-06)
- **Verificação:** com a aplicação rodando, `GET /v3/api-docs` lista todos os paths: `/login`, `/instrutores`, `/alunos`, `/usuarios`, `/instrucoes` e `/enderecos/{cep}`. Sem MySQL, valide com um teste MockMvc que faça `GET /v3/api-docs` → 200 e contenha esses paths.

### T8 — Seed de usuário administrador
- Criar a migration `V10__insert-usuario-admin.sql` com o login `admin`, a senha `admin` (hash BCrypt gerado com `shared/util/GerarSenhaHash`) e o perfil `ADMIN`. Documentar essas credenciais no README.
- **Credenciais do administrador padrão (seed V10):** login `admin`, senha `admin`, perfil `ADMIN`. Troque a senha em produção via `PUT /usuarios`.
- **Verificação:** build verde.

### T9 — Coleção de requisições
- Criar `docs/requests.http` (formato REST Client / IntelliJ HTTP) com exemplos de todos os endpoints, começando pelo login.

### T10 — Revisão final
- Rodar `./mvnw clean verify`.
- Conferir a seção 8 (Definição de Pronto) item por item.

### T11 — Swagger offline (opcional)
- Regenerar `SwaggerOffLine.yaml` (RF-06).

### T12 — Reescrever este README para a entrega
Substituir esta especificação por um README de entrega, com:
1. Título, disciplina e **nomes e RMs dos integrantes** (perguntar ao usuário se ainda estiver TODO);
2. Descrição e arquitetura, com a ADR-01 resumida;
3. Pré-requisitos (JDK 21 e MySQL 8) e o comando para criar o banco: `create database autoescola3esr;`;
4. Como rodar: `./mvnw spring-boot:run`. A porta é a **8081**;
5. Como testar: `./mvnw test`;
6. Credenciais de exemplo, a URL do Swagger (`http://localhost:8081/swagger-ui.html`) e como usar o token no botão "Authorize";
7. Tabela de endpoints, regras de negócio (RN-A e RN-C), a integração ViaCEP e a configuração de CORS;
8. Mapeamento dos requisitos E1 a E7 para os arquivos que os atendem.

Mova esta especificação para `docs/SPEC.md` antes de reescrever.

## 8. Definição de Pronto (checklist final)
- [ ] `./mvnw clean verify` → BUILD SUCCESS, sem MySQL
- [ ] CRUD de Instrutor, Aluno e Usuário funcionando, e Instrução com agendar, listar, detalhar e cancelar
- [ ] As 8 RN-A e as 4 RN-C implementadas e testadas
- [ ] `GET /enderecos/{cep}` consumindo o ViaCEP, com testes sem acesso à rede
- [ ] Swagger em `/swagger-ui.html` com todos os endpoints, tags e autenticação Bearer
- [ ] CORS configurável por propriedade, com teste
- [ ] Pelo menos um teste de controller por entidade (Instrutor, Aluno, Usuário e Instrução), além de testes de repositório e serviço
- [ ] Nenhum erro de cliente respondendo 500
- [ ] README final com os nomes dos integrantes e as instruções de execução
- [ ] Repositório público no GitHub (o push é feito pelo usuário, **não pelo agente**)

## 9. Fora de escopo
- Front-end.
- Deploy em nuvem e Docker. Docker é opcional e só deve ser feito se sobrar tempo.
- Refresh token, recuperação de senha ou cadastro público de usuários.
- Reagendamento (PUT) de instruções.
