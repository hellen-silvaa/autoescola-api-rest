package br.com.fiap3esr.autoescola3esr.adapter.out.client.viacep;

import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;

/**
 * Resposta do WebService ViaCEP. Quando o CEP não existe, o ViaCEP devolve apenas {@code {"erro": true}}.
 */
public record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        Boolean erro) {

    public boolean cepInexistente() {
        return Boolean.TRUE.equals(erro);
    }

    public DadosEndereco paraDadosEndereco() {
        return new DadosEndereco(logradouro, null, complemento, bairro, localidade, uf, cep);
    }
}
