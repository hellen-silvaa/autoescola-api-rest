package br.com.fiap3esr.autoescola3esr.application.service;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosCancelamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrucao.DadosDetalhamentoInstrucao;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrucao.DadosListagemInstrucao;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Aluno;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrucao;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrutor;
import br.com.fiap3esr.autoescola3esr.application.core.specification.contract.ValidadorAgendamento;
import br.com.fiap3esr.autoescola3esr.application.core.specification.contract.ValidadorCancelamento;
import br.com.fiap3esr.autoescola3esr.application.port.out.AlunoRepository;
import br.com.fiap3esr.autoescola3esr.application.port.out.InstrucaoRepository;
import br.com.fiap3esr.autoescola3esr.application.port.out.InstrutorRepository;
import br.com.fiap3esr.autoescola3esr.exception.type.AlunoNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.InstrucaoNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.InstrutorNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendaDeInstrucoes {
    private static final String INSTRUCAO_NAO_EXISTE = "ID da instrução informado não existe!";

    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;
    private final InstrucaoRepository repository;
    private final List<ValidadorAgendamento> validadoresAgendamento;
    private final List<ValidadorCancelamento> validadoresCancelamento;

    @Transactional
    public DadosDetalhamentoAgendamento agendar(DadosAgendamento dados) {
        if (!alunoRepository.existsById(dados.idAluno())) {
            throw new AlunoNotFoundException("ID do aluno informado não existe!");
        }
        if (dados.idInstrutor() != null && !instrutorRepository.existsById(dados.idInstrutor())) {
            throw new InstrutorNotFoundException("ID do instrutor informado não existe!");
        }
        //Validações
        validadoresAgendamento.forEach(validador -> validador.validar(dados));

        Aluno aluno = alunoRepository.getReferenceById(dados.idAluno());
        Instrutor instrutor = escolherInstrutor(dados);
        if (instrutor == null) {
            throw new ValidacaoException("Não existe instrutor disponível para a data/hora informada!");
        }
        Instrucao instrucao = new Instrucao(
                null,
                aluno,
                instrutor,
                dados.dataHora()
        );
        Instrucao salva = repository.save(instrucao);
        return new DadosDetalhamentoAgendamento(salva);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemInstrucao> listar(boolean somenteAtivas, Pageable paginacao) {
        Page<Instrucao> pagina = somenteAtivas
                ? repository.findAllByMotivoCancelamentoIsNull(paginacao)
                : repository.findAll(paginacao);
        return pagina.map(DadosListagemInstrucao::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoInstrucao detalhar(Long id) {
        Instrucao instrucao = repository.findById(id)
                .orElseThrow(() -> new InstrucaoNotFoundException(INSTRUCAO_NAO_EXISTE));
        return new DadosDetalhamentoInstrucao(instrucao);
    }

    @Transactional
    public void cancelar(DadosCancelamento dados) {
        if (!repository.existsById(dados.idInstrucao())) {
            throw new InstrucaoNotFoundException(INSTRUCAO_NAO_EXISTE);
        }
        //Validações
        validadoresCancelamento.forEach(validador -> validador.validar(dados));

        Instrucao instrucao = repository.getReferenceById(dados.idInstrucao());
        instrucao.cancelar(dados.motivo());
    }

    private Instrutor escolherInstrutor(DadosAgendamento dados) {
        if (dados.idInstrutor() != null) {
            return instrutorRepository.getReferenceById(dados.idInstrutor());
        }
        if (dados.especialidade() == null) {
            throw new ValidacaoException("Especialidade é campo obrigatório, caso o instrutor não seja informado!");
        }
        return instrutorRepository.escolherInstrutorAleatorioDisponivel(dados.especialidade(), dados.dataHora());
    }
}
