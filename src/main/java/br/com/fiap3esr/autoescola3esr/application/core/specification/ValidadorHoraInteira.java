package br.com.fiap3esr.autoescola3esr.application.core.specification;

import br.com.fiap3esr.autoescola3esr.application.core.specification.contract.ValidadorAgendamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ValidadorHoraInteira implements ValidadorAgendamento {
    @Override
    public void validar(DadosAgendamento dados) {
        LocalDateTime dataEscolhida = dados.dataHora();

        if (dataEscolhida.getMinute() != 0) {
            throw new ValidacaoException("O campo deve ser preenchido com hora inteira (ex: 09:00, 13:00, ...)");
        }
    }
}