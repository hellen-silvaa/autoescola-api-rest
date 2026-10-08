package br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.usuario;

import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroUsuario(
        @NotBlank
        String login,

        @NotBlank
        @Size(min = 6)
        String senha,

        Role perfil) {
}
