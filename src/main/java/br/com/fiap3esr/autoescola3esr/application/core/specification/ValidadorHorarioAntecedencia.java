package br.com.fiap3esr.autoescola3esr.application.core.specification;

import br.com.fiap3esr.autoescola3esr.application.core.specification.contract.ValidadorAgendamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ValidadorHorarioAntecedencia implements ValidadorAgendamento {
    @Override
    public void validar(DadosAgendamento dados) {
        LocalDateTime dataEscolhida = dados.dataHora();
        LocalDateTime agora = LocalDateTime.now();

        long antecedencia = Duration.between(agora, dataEscolhida).toMinutes();

        if (antecedencia < 30) {
            throw new ValidacaoException("O agendamento precisa ocorrer com antecedência mínima de 30 minutos!");
        }
    }
}