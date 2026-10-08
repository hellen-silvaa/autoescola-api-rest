package br.com.fiap3esr.autoescola3esr.application.port.out;

import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;

/**
 * Porta de saída para consulta de endereço a partir do CEP.
 */
public interface ConsultaCepGateway {

    /**
     * @param cep CEP já validado, com ou sem hífen (ex.: {@code 01001000} ou {@code 01001-000})
     * @return endereço correspondente, com {@code numero} nulo
     * @throws br.com.fiap3esr.autoescola3esr.exception.type.CepNotFoundException quando o CEP não existe
     * @throws br.com.fiap3esr.autoescola3esr.exception.type.IntegracaoExternaException quando o serviço externo falha
     */
    DadosEndereco consultar(String cep);
}
