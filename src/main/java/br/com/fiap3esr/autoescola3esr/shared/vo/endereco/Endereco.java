package br.com.fiap3esr.autoescola3esr.shared.vo.endereco;

import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Endereco {
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;

    public Endereco(DadosEndereco dados) {
        this.logradouro = dados.logradouro();
        this.numero = dados.numero();
        this.complemento = dados.complemento();
        this.bairro = dados.bairro();
        this.cidade = dados.cidade();
        this.uf = dados.uf();
        this.cep = dados.cep();
    }

    public void atualizarInformacoes(DadosEndereco dados) {
        if(dados.logradouro() != null && !dados.logradouro().isBlank()) {
            this.logradouro = dados.logradouro();
        }
        if(dados.numero() != null) {
            this.numero = dados.numero();
        }
        if(dados.complemento() != null) {
            this.complemento = dados.complemento();
        }
        if(dados.bairro() != null && !dados.bairro().isBlank()) {
            this.bairro = dados.bairro();
        }
        if(dados.cidade() != null && !dados.cidade().isBlank()) {
            this.cidade = dados.cidade();
        }
        if(dados.uf() != null && !dados.uf().isBlank()) {
            this.uf = dados.uf();
        }
        if(dados.cep() != null && !dados.cep().isBlank()) {
            this.cep = dados.cep();
        }
    }
}