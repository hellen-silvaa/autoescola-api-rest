package br.com.fiap3esr.autoescola3esr.application.core.specification.cancelamento;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosCancelamento;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrucao;
import br.com.fiap3esr.autoescola3esr.application.core.specification.contract.ValidadorCancelamento;
import br.com.fiap3esr.autoescola3esr.application.port.out.InstrucaoRepository;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * RN-C3: o cancelamento precisa ser feito com pelo menos 24 horas de antecedência.
 */
@Component
@RequiredArgsConstructor
public class ValidadorAntecedenciaCancelamento implements ValidadorCancelamento {
    private static final long ANTECEDENCIA_MINIMA_MINUTOS = 24 * 60;

    private final InstrucaoRepository repository;

    @Override
    public void validar(DadosCancelamento dados) {
        Instrucao instrucao = repository.getReferenceById(dados.idInstrucao());
        long antecedencia = Duration.between(LocalDateTime.now(), instrucao.getDataHora()).toMinutes();

        if (antecedencia < ANTECEDENCIA_MINIMA_MINUTOS) {
            throw new ValidacaoException("Instrução somente pode ser cancelada com antecedência mínima de 24 horas!");
        }
    }
}
