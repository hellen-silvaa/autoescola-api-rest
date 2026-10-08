package br.com.fiap3esr.autoescola3esr.application.core.specification;

import br.com.fiap3esr.autoescola3esr.application.core.specification.contract.ValidadorAgendamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import br.com.fiap3esr.autoescola3esr.application.port.out.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAlunoAtivo implements ValidadorAgendamento {
    @Autowired
    private AlunoRepository alunoRepository;

    @Override
    public void validar(DadosAgendamento dados) {
        if (alunoRepository.existsByIdAndAtivoFalse(dados.idAluno())) {
            throw new ValidacaoException("Instrução não pode ser agendada para aluno inativo!");
        }
    }
}