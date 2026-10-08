package br.com.fiap3esr.autoescola3esr.adapter.out.client.viacep;

import br.com.fiap3esr.autoescola3esr.application.port.out.ConsultaCepGateway;
import br.com.fiap3esr.autoescola3esr.exception.type.CepNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.IntegracaoExternaException;
import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Adapter do ViaCEP ({@code GET {base}/{cep}/json/}) usando o {@link RestClient} do Spring.
 */
@Component
public class ViaCepClient implements ConsultaCepGateway {

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    static final String MENSAGEM_NAO_ENCONTRADO = "CEP não encontrado!";
    static final String MENSAGEM_INDISPONIVEL = "Serviço de CEP indisponível no momento!";

    private final RestClient restClient;

    @Autowired
    public ViaCepClient(RestClient.Builder builder, @Value("${api.integracao.viacep.url}") String baseUrl) {
        this(builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactoryComTimeouts())
                .build());
    }

    /**
     * Construtor usado nos testes, que recebem um {@link RestClient} já ligado ao {@code MockRestServiceServer}.
     */
    ViaCepClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public DadosEndereco consultar(String cep) {
        String cepSomenteDigitos = cep.replace("-", "");

        ViaCepResponse resposta;
        try {
            resposta = restClient.get()
                    .uri("/{cep}/json/", cepSomenteDigitos)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ViaCepResponse.class);
        } catch (RestClientException e) {
            // falha de rede, timeout, status 4xx/5xx ou corpo ilegível
            throw new IntegracaoExternaException(MENSAGEM_INDISPONIVEL, e);
        }

        if (resposta == null) {
            throw new IntegracaoExternaException(MENSAGEM_INDISPONIVEL);
        }
        if (resposta.cepInexistente()) {
            throw new CepNotFoundException(MENSAGEM_NAO_ENCONTRADO);
        }
        return resposta.paraDadosEndereco();
    }

    private static SimpleClientHttpRequestFactory requestFactoryComTimeouts() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        return factory;
    }
}
