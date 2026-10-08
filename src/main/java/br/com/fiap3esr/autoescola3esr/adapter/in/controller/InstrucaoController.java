package br.com.fiap3esr.autoescola3esr.adapter.in.controller;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosCancelamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrucao.DadosDetalhamentoInstrucao;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrucao.DadosListagemInstrucao;
import br.com.fiap3esr.autoescola3esr.application.service.AgendaDeInstrucoes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/instrucoes")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Instruções", description = "Agendamento, consulta e cancelamento de instruções")
@RequiredArgsConstructor
public class InstrucaoController {
    private final AgendaDeInstrucoes agenda;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Agendar instrução",
            description = "Aplica as regras RN-A1 a RN-A8. Sem id_instrutor, a especialidade é obrigatória e o instrutor é escolhido aleatoriamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instrução agendada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Aluno ou instrutor não encontrado")})
    public ResponseEntity<DadosDetalhamentoAgendamento> agendarInstrucao(
            @RequestBody @Valid DadosAgendamento dados) {
        return ResponseEntity.ok(agenda.agendar(dados));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Listar instruções",
            description = "Lista paginada ordenada por data/hora. Com ativas=true, traz só as não canceladas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de instruções"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")})
    public ResponseEntity<Page<DadosListagemInstrucao>> listarInstrucoes(
            @Parameter(description = "Quando true, retorna apenas instruções não canceladas")
            @RequestParam(name = "ativas", defaultValue = "false") boolean ativas,
            @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable paginacao) {
        return ResponseEntity.ok(agenda.listar(ativas, paginacao));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Detalhar instrução", description = "Inclui o status (AGENDADA ou CANCELADA) e o motivo de cancelamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instrução encontrada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Instrução não encontrada")})
    public ResponseEntity<DadosDetalhamentoInstrucao> detalharInstrucao(@PathVariable Long id) {
        return ResponseEntity.ok(agenda.detalhar(id));
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Cancelar instrução", description = "Aplica as regras RN-C1 a RN-C4.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Instrução cancelada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Instrução não encontrada")})
    public ResponseEntity<Void> cancelarInstrucao(@RequestBody @Valid DadosCancelamento dados) {
        agenda.cancelar(dados);
        return ResponseEntity.noContent().build();
    }
}
