package br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrutor;

import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Especialidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DadosCadastroInstrutor(
        @NotBlank
        String nome,

        @NotBlank
        @Email
        String email,

        @NotNull
        String telefone,

        @NotBlank
        @Pattern(regexp = "\\d{9,11}")
        String cnh,

        @NotNull
        Especialidade especialidade,

        @Valid
        DadosEndereco endereco) {
}