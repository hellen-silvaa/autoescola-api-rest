package br.com.fiap3esr.autoescola3esr.application.core.specification.cancelamento;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosCancelamento;
import br.com.fiap3esr.autoescola3esr.application.core.specification.contract.ValidadorCancelamento;
import br.com.fiap3esr.autoescola3esr.application.port.out.InstrucaoRepository;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * RN-C4: não é permitido cancelar uma instrução já cancelada.
 */
@Component
@RequiredArgsConstructor
public class ValidadorInstrucaoJaCancelada implements ValidadorCancelamento {
    private final InstrucaoRepository repository;

    @Override
    public void validar(DadosCancelamento dados) {
        if (repository.getReferenceById(dados.idInstrucao()).isCancelada()) {
            throw new ValidacaoException("Instrução já está cancelada!");
        }
    }
}
