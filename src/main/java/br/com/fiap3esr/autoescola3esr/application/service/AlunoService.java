package br.com.fiap3esr.autoescola3esr.application.service;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.aluno.DadosAtualizacaoAluno;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.aluno.DadosCadastroAluno;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.aluno.DadosDetalhamentoAluno;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.aluno.DadosListagemAluno;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Aluno;
import br.com.fiap3esr.autoescola3esr.application.port.out.AlunoRepository;
import br.com.fiap3esr.autoescola3esr.exception.type.AlunoNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlunoService {
    private static final String MENSAGEM_ID_INEXISTENTE = "ID do Aluno informado não existe!";

    private final AlunoRepository repository;

    @Transactional
    public DadosDetalhamentoAluno cadastrarAluno(DadosCadastroAluno dados) {
        Aluno aluno = new Aluno(dados);
        Aluno saved = repository.save(aluno);
        return new DadosDetalhamentoAluno(saved);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemAluno> listarAlunos(Pageable paginacao) {
        return repository
                .findAllByAtivoTrue(paginacao)
                .map(DadosListagemAluno::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoAluno detalharAluno(Long id) {
        return new DadosDetalhamentoAluno(buscarAluno(id));
    }

    @Transactional
    public DadosDetalhamentoAluno atualizarAluno(DadosAtualizacaoAluno dados) {
        Aluno aluno = buscarAluno(dados.id());
        aluno.atualizarInformacoes(dados);
        Aluno saved = repository.save(aluno);
        return new DadosDetalhamentoAluno(saved);
    }

    @Transactional
    public void excluirAluno(Long id) {
        Aluno aluno = buscarAluno(id);
        aluno.excluir();
        repository.save(aluno);
    }

    private Aluno buscarAluno(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new AlunoNotFoundException(MENSAGEM_ID_INEXISTENTE));
    }
}
