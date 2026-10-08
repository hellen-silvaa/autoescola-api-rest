package br.com.fiap3esr.autoescola3esr.controller;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.aluno.DadosDetalhamentoAluno;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.aluno.DadosListagemAluno;
import br.com.fiap3esr.autoescola3esr.application.service.AlunoService;
import br.com.fiap3esr.autoescola3esr.exception.type.AlunoNotFoundException;
import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Expectativa: AlunoController responde conforme o RF-02.")
public class AlunoControllerTest {
    private static final String JSON_CADASTRO_VALIDO = """
            {
              "nome": "Ana Silva",
              "email": "ana@email.com",
              "telefone": "11999999999",
              "cpf": "12345678901",
              "endereco": {
                "logradouro": "Rua A",
                "numero": "10",
                "bairro": "Centro",
                "cidade": "São Paulo",
                "uf": "SP",
                "cep": "01234-567"
              }
            }
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AlunoService service;

    @Test
    @DisplayName("Expectativa: Retornar 201 com Location ao cadastrar aluno válido como ADMIN.")
    @WithMockUser(roles = "ADMIN")
    void cadastrarAlunoCenario1() throws Exception {
        //arrange
        when(service.cadastrarAluno(any())).thenReturn(detalhamento(1L));

        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CADASTRO_VALIDO))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getHeader("Location")).endsWith("/alunos/1");
        assertThat(response.getContentAsString()).contains("\"cpf\":\"12345678901\"");
    }

    @Test
    @DisplayName("Expectativa: Retornar 400 quando o CPF tiver 10 dígitos.")
    @WithMockUser(roles = "ADMIN")
    void cadastrarAlunoCenario2() throws Exception {
        //arrange
        String json = JSON_CADASTRO_VALIDO.replace("12345678901", "1234567890");

        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("cpf");
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Expectativa: Retornar 403 quando USER tentar cadastrar aluno.")
    @WithMockUser(roles = "USER")
    void cadastrarAlunoCenario3() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CADASTRO_VALIDO))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Expectativa: Retornar 401 ou 403 ao cadastrar aluno sem token.")
    void cadastrarAlunoCenario4() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CADASTRO_VALIDO))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Expectativa: Retornar 200 ao listar alunos como USER.")
    @WithMockUser(roles = "USER")
    void listarAlunosCenario1() throws Exception {
        //arrange
        when(service.listarAlunos(any())).thenReturn(new PageImpl<>(List.of(
                new DadosListagemAluno(1L, "Ana Silva", "ana@email.com", "12345678901"))));

        //act
        MockHttpServletResponse response = mockMvc
                .perform(get("/alunos"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).contains("Ana Silva");
    }

    @Test
    @DisplayName("Expectativa: Retornar 404 ao detalhar aluno com ID inexistente.")
    @WithMockUser(roles = "ADMIN")
    void detalharAlunoCenario1() throws Exception {
        //arrange
        when(service.detalharAluno(99L))
                .thenThrow(new AlunoNotFoundException("ID do Aluno informado não existe!"));

        //act
        MockHttpServletResponse response = mockMvc
                .perform(get("/alunos/99"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).contains("mensagem");
    }

    @Test
    @DisplayName("Expectativa: Retornar 200 ao atualizar aluno como ADMIN.")
    @WithMockUser(roles = "ADMIN")
    void atualizarAlunoCenario1() throws Exception {
        //arrange
        when(service.atualizarAluno(any())).thenReturn(detalhamento(1L));
        String json = """
                {"id": 1, "nome": "Ana Silva"}
                """;

        //act
        MockHttpServletResponse response = mockMvc
                .perform(put("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Expectativa: Retornar 204 ao excluir aluno como ADMIN.")
    @WithMockUser(roles = "ADMIN")
    void excluirAlunoCenario1() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(delete("/alunos/1"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(service).excluirAluno(1L);
    }

    @Test
    @DisplayName("Expectativa: Retornar 404 ao excluir aluno com ID inexistente.")
    @WithMockUser(roles = "ADMIN")
    void excluirAlunoCenario2() throws Exception {
        //arrange
        doThrow(new AlunoNotFoundException("ID do Aluno informado não existe!"))
                .when(service).excluirAluno(99L);

        //act
        MockHttpServletResponse response = mockMvc
                .perform(delete("/alunos/99"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private DadosDetalhamentoAluno detalhamento(Long id) {
        return new DadosDetalhamentoAluno(
                id,
                "Ana Silva",
                "ana@email.com",
                "11999999999",
                "12345678901",
                new DadosEndereco("Rua A", "10", null, "Centro", "São Paulo", "SP", "01234-567"),
                true
        );
    }
}
