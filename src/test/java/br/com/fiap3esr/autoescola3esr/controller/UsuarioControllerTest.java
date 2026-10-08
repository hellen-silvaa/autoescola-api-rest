package br.com.fiap3esr.autoescola3esr.controller;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.usuario.DadosCadastroUsuario;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.usuario.DadosDetalhamentoUsuario;
import br.com.fiap3esr.autoescola3esr.application.service.UsuarioService;
import br.com.fiap3esr.autoescola3esr.exception.type.UsuarioNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Expectativa: O CRUD de usuários respeita perfis, validações e nunca expõe a senha.")
class UsuarioControllerTest {
    private static final String JSON_CADASTRO = """
            {"login":"joao","senha":"segredo123","perfil":"USER"}
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UsuarioService service;

    @Test
    @DisplayName("Expectativa: ADMIN cria usuário → 201 com Location e a resposta não contém senha.")
    @WithMockUser(roles = "ADMIN")
    void cadastrarCenario1() throws Exception {
        //arrange
        when(service.cadastrarUsuario(any(DadosCadastroUsuario.class)))
                .thenReturn(new DadosDetalhamentoUsuario(1L, "joao", Role.USER));

        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(JSON_CADASTRO))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getHeader("Location")).endsWith("/usuarios/1");
        assertThat(response.getContentAsString()).contains("\"login\":\"joao\"");
        assertThat(response.getContentAsString()).doesNotContain("senha").doesNotContain("segredo123");
    }

    @Test
    @DisplayName("Expectativa: USER tenta criar usuário → 403 e o service não é chamado.")
    @WithMockUser(roles = "USER")
    void cadastrarCenario2() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(JSON_CADASTRO))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Expectativa: Login duplicado → 400 com a mensagem \"Login já cadastrado!\".")
    @WithMockUser(roles = "ADMIN")
    void cadastrarCenario3() throws Exception {
        //arrange
        when(service.cadastrarUsuario(any(DadosCadastroUsuario.class)))
                .thenThrow(new ValidacaoException("Login já cadastrado!"));

        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(JSON_CADASTRO))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        response.setCharacterEncoding("UTF-8");
        assertThat(response.getContentAsString()).contains("Login já cadastrado!");
    }

    @Test
    @DisplayName("Expectativa: Senha com menos de 6 caracteres → 400.")
    @WithMockUser(roles = "ADMIN")
    void cadastrarCenario4() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"joao","senha":"123"}
                                """))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Expectativa: Sem token → 401 ou 403.")
    void cadastrarCenario5() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(JSON_CADASTRO))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Expectativa: Detalhar ID inexistente → 404.")
    @WithMockUser(roles = "ADMIN")
    void detalharCenario1() throws Exception {
        //arrange
        when(service.detalharUsuario(99L))
                .thenThrow(new UsuarioNotFoundException("ID do Usuário informado não existe!"));

        //act
        MockHttpServletResponse response = mockMvc
                .perform(get("/usuarios/99"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Expectativa: ADMIN tenta excluir o próprio usuário → 400.")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void excluirCenario1() throws Exception {
        //arrange
        doThrow(new ValidacaoException("Não é permitido excluir o próprio usuário logado!"))
                .when(service).excluirUsuario(1L, "admin");

        //act
        MockHttpServletResponse response = mockMvc
                .perform(delete("/usuarios/1"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(service).excluirUsuario(eq(1L), eq("admin"));
    }

    @Test
    @DisplayName("Expectativa: ADMIN exclui outro usuário → 204.")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void excluirCenario2() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(delete("/usuarios/2"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(service).excluirUsuario(2L, "admin");
    }
}
