package br.com.fiap3esr.autoescola3esr.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Expectativa: Documentação OpenAPI pública e com os paths da API.")
class OpenApiDocsTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Expectativa: Retornar código 200 sem autenticação, contendo os paths /instrutores e /login.")
    void apiDocsCenario1() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(get("/v3/api-docs"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        String json = response.getContentAsString();
        assertThat(json).contains("\"/instrutores\"", "\"/login\"", "bearer-key");
    }

    @Test
    @DisplayName("Expectativa: Documentar os paths /alunos, /usuarios, /instrucoes e /enderecos/{cep}.")
    void apiDocsCenario2() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(get("/v3/api-docs"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        String json = response.getContentAsString();
        assertThat(json).contains(
                "\"/alunos\"",
                "\"/alunos/{id}\"",
                "\"/usuarios\"",
                "\"/usuarios/{id}\"",
                "\"/instrucoes\"",
                "\"/instrucoes/{id}\"",
                "\"/enderecos/{cep}\"",
                "\"/instrutores/{id}\"");
    }

    @Test
    @DisplayName("Expectativa: Retornar código 200 sem autenticação no health check.")
    void healthCheckCenario1() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(get("/health-check"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}
