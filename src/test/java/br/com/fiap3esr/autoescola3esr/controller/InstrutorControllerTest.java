package br.com.fiap3esr.autoescola3esr.controller;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrutor.DadosDetalhamentoInstrutor;
import br.com.fiap3esr.autoescola3esr.application.service.InstrutorService;
import br.com.fiap3esr.autoescola3esr.exception.type.InstrutorNotFoundException;
import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Especialidade;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Expectativa: InstrutorController responde conforme o RF-01.")
class InstrutorControllerTest {
    private static final String JSON_CADASTRO_VALIDO = """
            {
              "nome": "Carlos Instrutor",
              "email": "carlos@email.com",
              "telefone": "11988887777",
              "cnh": "01234567890",
              "especialidade": "CARROS",
              "endereco": {
                "logradouro": "Rua B",
                "numero": "20",
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
    InstrutorService service;

    @Test
    @DisplayName("Expectativa: Retornar 201 com Location ao cadastrar instrutor válido como ADMIN.")
    @WithMockUser(roles = "ADMIN")
    void cadastrarInstrutorCenario1() throws Exception {
        //arrange
        when(service.cadastrarInstrutor(any())).thenReturn(detalhamento(1L));

        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/instrutores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CADASTRO_VALIDO))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getHeader("Location")).endsWith("/instrutores/1");
    }

    @Test
    @DisplayName("Expectativa: Retornar 400 quando o corpo do cadastro for inválido.")
    @WithMockUser(roles = "ADMIN")
    void cadastrarInstrutorCenario2() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/instrutores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Expectativa: Retornar 403 quando USER tentar cadastrar instrutor.")
    @WithMockUser(roles = "USER")
    void cadastrarInstrutorCenario3() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/instrutores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CADASTRO_VALIDO))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Expectativa: Retornar 401 ou 403 ao cadastrar instrutor sem token.")
    void cadastrarInstrutorCenario4() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/instrutores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CADASTRO_VALIDO))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Expectativa: Retornar 404 ao detalhar instrutor com ID inexistente.")
    @WithMockUser(roles = "ADMIN")
    void detalharInstrutorCenario1() throws Exception {
        //arrange
        when(service.detalharInstrutor(99L))
                .thenThrow(new InstrutorNotFoundException("ID do Instrutor informado não existe!"));

        //act
        MockHttpServletResponse response = mockMvc
                .perform(get("/instrutores/99"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).contains("mensagem");
    }

    private DadosDetalhamentoInstrutor detalhamento(Long id) {
        return new DadosDetalhamentoInstrutor(
                id,
                "Carlos Instrutor",
                "carlos@email.com",
                "11988887777",
                "01234567890",
                Especialidade.CARROS,
                new DadosEndereco("Rua B", "20", null, "Centro", "São Paulo", "SP", "01234-567"),
                true
        );
    }
}
