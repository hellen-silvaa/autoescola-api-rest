package br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrutor;

import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrutor;
import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Especialidade;

public record DadosDetalhamentoInstrutor(
        Long id,
        String nome,
        String email,
        String telefone,
        String cnh,
        Especialidade especialidade,
        DadosEndereco endereco,
        boolean ativo) {
    public DadosDetalhamentoInstrutor(Instrutor instrutor) {
        this(
                instrutor.getId(),
                instrutor.getNome(),
                instrutor.getEmail(),
                instrutor.getTelefone(),
                instrutor.getCnh(),
                instrutor.getEspecialidade(),
                new DadosEndereco(instrutor.getEndereco()),
                instrutor.isAtivo()
        );
    }
}