package br.com.fiap3esr.autoescola3esr.exception;

import br.com.fiap3esr.autoescola3esr.exception.type.IntegracaoExternaException;
import br.com.fiap3esr.autoescola3esr.exception.type.RecursoNaoEncontradoException;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Tratamento centralizado de exceções. Corpo padrão de erro: {@code { "mensagem": "..." }}.
 * <p>
 * Exceções novas devem estender uma das bases já mapeadas
 * ({@link RecursoNaoEncontradoException} → 404, {@link ValidacaoException} → 400,
 * {@link IntegracaoExternaException} → 503), sem necessidade de alterar este handler.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - erros de validação de campos (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosErroValidacao>> tratarErroValidacao(MethodArgumentNotValidException e) {
        List<FieldError> erros = e.getFieldErrors();
        return ResponseEntity.badRequest().body(erros.stream().map(DadosErroValidacao::new).toList());
    }

    // 400 - regra de negócio violada
    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<DadosErro> tratarRegraDeNegocio(ValidacaoException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 400 - corpo ausente ou JSON malformado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DadosErro> tratarCorpoInvalido() {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou inválido!");
    }

    // 400 - parâmetro de rota/consulta com tipo inválido (ex.: /instrutores/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<DadosErro> tratarTipoInvalido(MethodArgumentTypeMismatchException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro '" + e.getName() + "' com valor inválido!");
    }

    // 400 - violação de restrição do banco (ex.: e-mail/CPF/CNH duplicado)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DadosErro> tratarIntegridade() {
        return resposta(HttpStatus.BAD_REQUEST, "Dados informados conflitam com registros existentes!");
    }

    // 401 - credenciais inválidas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<DadosErro> tratarCredenciaisInvalidas() {
        return resposta(HttpStatus.UNAUTHORIZED, "Login ou senha inválidos!");
    }

    // 401 - demais falhas de autenticação
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DadosErro> tratarFalhaAutenticacao() {
        return resposta(HttpStatus.UNAUTHORIZED, "Falha na autenticação!");
    }

    // 403 - sem permissão (inclui @PreAuthorize negado)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DadosErro> tratarAcessoNegado() {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado!");
    }

    // 404 - recurso inexistente
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<DadosErro> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 404 - getReferenceById de ID inexistente
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<DadosErro> tratarEntidadeNaoEncontrada() {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado!");
    }

    // 503 - falha em integração externa (ex.: ViaCEP fora do ar)
    @ExceptionHandler(IntegracaoExternaException.class)
    public ResponseEntity<DadosErro> tratarIntegracaoExterna(IntegracaoExternaException e) {
        log.warn("Falha em integração externa: {}", e.getMessage(), e.getCause());
        return resposta(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    // 500 - qualquer erro inesperado, com mensagem neutra.
    // Exceções do Spring MVC que já carregam o status correto (405, 415, 404 de rota, etc.)
    // mantêm esse status, para que erro de cliente nunca vire 500.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DadosErro> tratarErroInesperado(Exception e) {
        if (e instanceof ErrorResponse errorResponse) {
            return tratarErroSpringMvc(errorResponse.getStatusCode());
        }
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor!");
    }

    private ResponseEntity<DadosErro> tratarErroSpringMvc(HttpStatusCode status) {
        String mensagem = switch (status.value()) {
            case 400 -> "Requisição inválida!";
            case 404 -> "Recurso não encontrado!";
            case 405 -> "Método HTTP não suportado para este recurso!";
            case 406 -> "Formato de resposta não suportado!";
            case 415 -> "Tipo de conteúdo não suportado!";
            default -> status.is5xxServerError()
                    ? "Erro interno no servidor!"
                    : "Requisição não pôde ser processada!";
        };
        return ResponseEntity.status(status).body(new DadosErro(mensagem));
    }

    private ResponseEntity<DadosErro> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new DadosErro(mensagem));
    }

    public record DadosErro(String mensagem) {
    }

    public record DadosErroValidacao(String campo, String mensagem) {
        public DadosErroValidacao(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }
}
