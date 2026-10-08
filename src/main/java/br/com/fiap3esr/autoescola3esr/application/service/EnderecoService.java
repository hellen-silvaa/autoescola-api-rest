package br.com.fiap3esr.autoescola3esr.application.service;

import br.com.fiap3esr.autoescola3esr.application.port.out.ConsultaCepGateway;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private static final Pattern FORMATO_CEP = Pattern.compile("\\d{5}-?\\d{3}");

    private final ConsultaCepGateway gateway;

    public DadosEndereco consultarPorCep(String cep) {
        return gateway.consultar(normalizarCep(cep));
    }

    /**
     * Valida o formato {@code \d{5}-?\d{3}} e devolve apenas os 8 dígitos.
     */
    static String normalizarCep(String cep) {
        String valor = cep == null ? "" : cep.trim();
        if (!FORMATO_CEP.matcher(valor).matches()) {
            throw new ValidacaoException("CEP inválido!");
        }
        return valor.replace("-", "");
    }
}
