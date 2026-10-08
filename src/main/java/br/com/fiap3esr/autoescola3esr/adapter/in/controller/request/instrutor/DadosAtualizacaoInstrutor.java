package br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrutor;

import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Especialidade;
import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoInstrutor(
        @NotNull
        Long id,
        String nome,
        String email,
        String telefone,
        Especialidade especialidade,
        DadosEndereco endereco) {
}