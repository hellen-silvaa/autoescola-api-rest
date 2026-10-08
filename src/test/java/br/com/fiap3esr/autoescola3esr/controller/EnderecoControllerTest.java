package br.com.fiap3esr.autoescola3esr.controller;

import br.com.fiap3esr.autoescola3esr.application.port.out.ConsultaCepGateway;
import br.com.fiap3esr.autoescola3esr.exception.type.CepNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.IntegracaoExternaException;
import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Expectativa: GET /enderecos/{cep} consulta o ViaCEP (gateway mockado) e traduz os erros em status HTTP.")
class EnderecoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ConsultaCepGateway gateway;

    private DadosEndereco enderecoSe() {
        return new DadosEndereco("Praça da Sé", null, "lado ímpar", "Sé", "São Paulo", "SP", "01001-000");
    }

    @Test
    @DisplayName("Expectativa: Retornar 200 com o endereço para um USER e CEP com hífen.")
    @WithMockUser(roles = "USER")
    void consultarCenario1() throws Exception {
        //arrange
        when(gateway.consultar("01001000")).thenReturn(enderecoSe());

        //act
        MockHttpServletResponse response = mockMvc.perform(get("/enderecos/01001-000"))
                .andReturn().getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        String corpo = response.getContentAsString(StandardCharsets.UTF_8);
        assertThat(corpo).contains("\"cidade\":\"São Paulo\"").contains("\"cep\":\"01001-000\"");
        verify(gateway).consultar("01001000");
    }

    @Test
    @DisplayName("Expectativa: Retornar 200 para um ADMIN e CEP sem hífen.")
    @WithMockUser(roles = "ADMIN")
    void consultarCenario2() throws Exception {
        //arrange
        when(gateway.consultar("01001000")).thenReturn(enderecoSe());

        //act
        MockHttpServletResponse response = mockMvc.perform(get("/enderecos/01001000"))
                .andReturn().getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Expectativa: Retornar 400 com \"CEP inválido!\" quando o formato for inválido, sem chamar o gateway.")
    @WithMockUser(roles = "USER")
    void consultarCenario3() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc.perform(get("/enderecos/1234-567"))
                .andReturn().getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString(StandardCharsets.UTF_8)).contains("CEP inválido!");
        verifyNoInteractions(gateway);
    }

    @Test
    @DisplayName("Expectativa: Retornar 404 com \"CEP não encontrado!\" quando o CEP não existir.")
    @WithMockUser(roles = "USER")
    void consultarCenario4() throws Exception {
        //arrange
        when(gateway.consultar("99999999")).thenThrow(new CepNotFoundException("CEP não encontrado!"));

        //act
        MockHttpServletResponse response = mockMvc.perform(get("/enderecos/99999-999"))
                .andReturn().getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString(StandardCharsets.UTF_8)).contains("CEP não encontrado!");
    }

    @Test
    @DisplayName("Expectativa: Retornar 503 quando o ViaCEP estiver indisponível.")
    @WithMockUser(roles = "ADMIN")
    void consultarCenario5() throws Exception {
        //arrange
        when(gateway.consultar("01001000"))
                .thenThrow(new IntegracaoExternaException("Serviço de CEP indisponível no momento!"));

        //act
        MockHttpServletResponse response = mockMvc.perform(get("/enderecos/01001000"))
                .andReturn().getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(response.getContentAsString(StandardCharsets.UTF_8))
                .contains("Serviço de CEP indisponível no momento!");
    }

    @Test
    @DisplayName("Expectativa: Retornar 401 ou 403 sem token, sem chamar o gateway.")
    void consultarCenario6() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc.perform(get("/enderecos/01001000"))
                .andReturn().getResponse();

        //assert
        assertThat(response.getStatus()).isIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
        verifyNoInteractions(gateway);
    }
}
