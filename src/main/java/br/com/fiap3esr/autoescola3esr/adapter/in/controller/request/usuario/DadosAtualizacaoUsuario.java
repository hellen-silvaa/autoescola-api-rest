package br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.usuario;

import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoUsuario(
        @NotNull
        Long id,

        @Size(min = 6)
        String senha,

        Role perfil) {
}
