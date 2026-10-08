package br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto;

import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DadosEndereco(
        @NotBlank
        String logradouro,
        String numero,
        String complemento,

        @NotBlank
        String bairro,

        @NotBlank
        String cidade,

        @NotBlank
        @Pattern(regexp = "[A-Z]{2}")
        String uf,

        @NotBlank
        @Pattern(regexp = "[0-9]{5}-[0-9]{3}")
        String cep) {
        public DadosEndereco(Endereco endereco) {
                this(
                        endereco.getLogradouro(),
                        endereco.getNumero(),
                        endereco.getComplemento(),
                        endereco.getBairro(),
                        endereco.getCidade(),
                        endereco.getUf(),
                        endereco.getCep()
                );
        }
}