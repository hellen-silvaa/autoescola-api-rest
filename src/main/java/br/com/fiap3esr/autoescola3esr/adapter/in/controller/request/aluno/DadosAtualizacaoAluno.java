package br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.aluno;

import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoAluno(
        @NotNull
        Long id,
        String nome,

        @Email
        String email,
        String telefone,
        DadosEndereco endereco) {
}
