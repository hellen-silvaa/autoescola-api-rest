package br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.aluno;

import br.com.fiap3esr.autoescola3esr.application.core.domain.Aluno;

public record DadosListagemAluno(
        Long id,
        String nome,
        String email,
        String cpf) {
    public DadosListagemAluno(Aluno aluno) {
        this(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getCpf()
        );
    }
}
