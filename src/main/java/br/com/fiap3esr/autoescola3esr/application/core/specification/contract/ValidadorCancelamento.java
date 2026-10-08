package br.com.fiap3esr.autoescola3esr.application.core.specification.contract;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosCancelamento;

public interface ValidadorCancelamento {
    void validar(DadosCancelamento dados);
}
