package br.com.fiap3esr.autoescola3esr.adapter.out.client.viacep;

import br.com.fiap3esr.autoescola3esr.exception.type.CepNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.IntegracaoExternaException;
import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Testa o adapter do ViaCEP com {@link MockRestServiceServer} ligado a um {@link RestClient.Builder}
 * (alternativa sem slice ao {@code @RestClientTest}). Nenhuma chamada sai para a internet.
 */
@DisplayName("Expectativa: ViaCepClient mapeia as respostas do ViaCEP e traduz as falhas em exceções da aplicação.")
class ViaCepClientTest {

    private static final String BASE_URL = "http://viacep.teste/ws";

    private MockRestServiceServer server;
    private ViaCepClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ViaCepClient(builder.build());
    }

    @Test
    @DisplayName("Expectativa: Retornar o DadosEndereco mapeado quando o CEP existir.")
    void consultarCenario1() {
        //arrange
        String json = """
                {
                  "cep": "01001-000",
                  "logradouro": "Praça da Sé",
                  "complemento": "lado ímpar",
                  "bairro": "Sé",
                  "localidade": "São Paulo",
                  "uf": "SP",
                  "ibge": "3550308"
                }
                """;
        server.expect(requestTo(BASE_URL + "/01001000/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        //act
        DadosEndereco endereco = client.consultar("01001-000");

        //assert
        server.verify();
        assertThat(endereco.logradouro()).isEqualTo("Praça da Sé");
        assertThat(endereco.numero()).isNull();
        assertThat(endereco.complemento()).isEqualTo("lado ímpar");
        assertThat(endereco.bairro()).isEqualTo("Sé");
        assertThat(endereco.cidade()).isEqualTo("São Paulo");
        assertThat(endereco.uf()).isEqualTo("SP");
        assertThat(endereco.cep()).isEqualTo("01001-000");
    }

    @Test
    @DisplayName("Expectativa: Lançar CepNotFoundException quando o ViaCEP responder {\"erro\": true}.")
    void consultarCenario2() {
        //arrange
        server.expect(requestTo(BASE_URL + "/99999999/json/"))
                .andRespond(withSuccess("""
                        {"erro": true}
                        """, MediaType.APPLICATION_JSON));

        //act + assert
        assertThatThrownBy(() -> client.consultar("99999999"))
                .isInstanceOf(CepNotFoundException.class)
                .hasMessage("CEP não encontrado!");
        server.verify();
    }

    @Test
    @DisplayName("Expectativa: Lançar IntegracaoExternaException quando o ViaCEP responder 500.")
    void consultarCenario3() {
        //arrange
        server.expect(requestTo(BASE_URL + "/01001000/json/"))
                .andRespond(withServerError());

        //act + assert
        assertThatThrownBy(() -> client.consultar("01001000"))
                .isInstanceOf(IntegracaoExternaException.class)
                .hasMessage("Serviço de CEP indisponível no momento!");
        server.verify();
    }

    @Test
    @DisplayName("Expectativa: Lançar IntegracaoExternaException quando houver falha de rede.")
    void consultarCenario4() {
        //arrange
        server.expect(requestTo(BASE_URL + "/01001000/json/"))
                .andRespond(withException(new IOException("Connection refused")));

        //act + assert
        assertThatThrownBy(() -> client.consultar("01001000"))
                .isInstanceOf(IntegracaoExternaException.class)
                .hasMessage("Serviço de CEP indisponível no momento!");
        server.verify();
    }
}
