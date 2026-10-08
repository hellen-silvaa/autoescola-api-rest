package br.com.fiap3esr.autoescola3esr.application.core.specification;

import br.com.fiap3esr.autoescola3esr.application.core.specification.contract.ValidadorAgendamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import br.com.fiap3esr.autoescola3esr.application.port.out.InstrutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorInstrutorAtivo implements ValidadorAgendamento {
    @Autowired
    private InstrutorRepository instrutorRepository;

    @Override
    public void validar(DadosAgendamento dados) {
        if (instrutorRepository.existsByIdAndAtivoFalse(dados.idInstrutor())) {
            throw new ValidacaoException("Instrução não pode ser agendada com instrutor inativo!");
        }
    }
}