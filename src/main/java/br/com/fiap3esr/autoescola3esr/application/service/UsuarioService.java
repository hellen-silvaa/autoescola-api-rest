package br.com.fiap3esr.autoescola3esr.application.service;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.usuario.DadosAtualizacaoUsuario;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.usuario.DadosCadastroUsuario;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.usuario.DadosDetalhamentoUsuario;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.usuario.DadosListagemUsuario;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Usuario;
import br.com.fiap3esr.autoescola3esr.application.port.out.UsuarioRepository;
import br.com.fiap3esr.autoescola3esr.exception.type.UsuarioNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DadosDetalhamentoUsuario cadastrarUsuario(DadosCadastroUsuario dados) {
        if (repository.existsByLogin(dados.login())) {
            throw new ValidacaoException("Login já cadastrado!");
        }
        String senhaHash = passwordEncoder.encode(dados.senha());
        Usuario usuario = new Usuario(dados.login(), senhaHash, dados.perfil());
        Usuario saved = repository.save(usuario);
        return new DadosDetalhamentoUsuario(saved);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemUsuario> listarUsuarios(Pageable paginacao) {
        return repository
                .findAll(paginacao)
                .map(DadosListagemUsuario::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoUsuario detalharUsuario(Long id) {
        return new DadosDetalhamentoUsuario(buscarUsuario(id));
    }

    @Transactional
    public DadosDetalhamentoUsuario atualizarUsuario(DadosAtualizacaoUsuario dados) {
        Usuario usuario = buscarUsuario(dados.id());
        String senhaHash = dados.senha() != null && !dados.senha().isBlank()
                ? passwordEncoder.encode(dados.senha())
                : null;
        usuario.atualizar(senhaHash, dados.perfil());
        Usuario saved = repository.save(usuario);
        return new DadosDetalhamentoUsuario(saved);
    }

    @Transactional
    public void excluirUsuario(Long id, String loginUsuarioLogado) {
        Usuario usuario = buscarUsuario(id);
        if (usuario.getLogin().equals(loginUsuarioLogado)) {
            throw new ValidacaoException("Não é permitido excluir o próprio usuário logado!");
        }
        repository.delete(usuario);
    }

    private Usuario buscarUsuario(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() ->
                        new UsuarioNotFoundException("ID do Usuário informado não existe!"));
    }
}
