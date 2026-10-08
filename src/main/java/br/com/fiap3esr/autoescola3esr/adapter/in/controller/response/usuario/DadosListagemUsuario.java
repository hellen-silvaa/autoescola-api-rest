package br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.usuario;

import br.com.fiap3esr.autoescola3esr.application.core.domain.Usuario;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Role;

public record DadosListagemUsuario(
        Long id,
        String login,
        Role perfil) {
    public DadosListagemUsuario(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getLogin(),
                usuario.getPerfil()
        );
    }
}
