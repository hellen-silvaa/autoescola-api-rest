package br.com.fiap3esr.autoescola3esr.service;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.usuario.DadosAtualizacaoUsuario;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.usuario.DadosCadastroUsuario;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.usuario.DadosDetalhamentoUsuario;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Usuario;
import br.com.fiap3esr.autoescola3esr.application.port.out.UsuarioRepository;
import br.com.fiap3esr.autoescola3esr.application.service.UsuarioService;
import br.com.fiap3esr.autoescola3esr.exception.type.UsuarioNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Expectativa: O UsuarioService grava senhas em hash e aplica as regras de usuário.")
class UsuarioServiceTest {
    @Mock
    UsuarioRepository repository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UsuarioService service;

    @Test
    @DisplayName("Expectativa: A senha é gravada como hash gerado pelo PasswordEncoder.")
    void cadastrarCenario1() {
        //arrange
        when(repository.existsByLogin("joao")).thenReturn(false);
        when(passwordEncoder.encode("segredo123")).thenReturn("HASH");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        //act
        service.cadastrarUsuario(new DadosCadastroUsuario("joao", "segredo123", Role.ADMIN));

        //assert
        verify(passwordEncoder).encode("segredo123");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getSenha()).isEqualTo("HASH");
        assertThat(captor.getValue().getPerfil()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Expectativa: Sem perfil informado, o usuário é criado como USER.")
    void cadastrarCenario2() {
        //arrange
        when(repository.existsByLogin("maria")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("HASH");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        //act
        DadosDetalhamentoUsuario dto = service.cadastrarUsuario(new DadosCadastroUsuario("maria", "segredo123", null));

        //assert
        assertThat(dto.perfil()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("Expectativa: Login duplicado lança ValidacaoException e não salva.")
    void cadastrarCenario3() {
        //arrange
        when(repository.existsByLogin("admin")).thenReturn(true);

        //act + assert
        assertThatThrownBy(() -> service.cadastrarUsuario(new DadosCadastroUsuario("admin", "segredo123", null)))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Login já cadastrado!");
        verify(repository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Expectativa: Atualizar troca a senha por um novo hash e altera o perfil.")
    void atualizarCenario1() {
        //arrange
        Usuario usuario = new Usuario(1L, "joao", "HASH_ANTIGO", Role.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha")).thenReturn("HASH_NOVO");
        when(repository.save(usuario)).thenReturn(usuario);

        //act
        service.atualizarUsuario(new DadosAtualizacaoUsuario(1L, "novaSenha", Role.ADMIN));

        //assert
        assertThat(usuario.getSenha()).isEqualTo("HASH_NOVO");
        assertThat(usuario.getPerfil()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Expectativa: Excluir o próprio usuário logado lança ValidacaoException.")
    void excluirCenario1() {
        //arrange
        Usuario usuario = new Usuario(1L, "admin", "HASH", Role.ADMIN);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        //act + assert
        assertThatThrownBy(() -> service.excluirUsuario(1L, "admin"))
                .isInstanceOf(ValidacaoException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Expectativa: Excluir ID inexistente lança UsuarioNotFoundException.")
    void excluirCenario2() {
        //arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        //act + assert
        assertThatThrownBy(() -> service.excluirUsuario(99L, "admin"))
                .isInstanceOf(UsuarioNotFoundException.class);
    }
}
