package br.com.fiap3esr.autoescola3esr.specification;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosCancelamento;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrucao;
import br.com.fiap3esr.autoescola3esr.application.core.specification.cancelamento.ValidadorAntecedenciaCancelamento;
import br.com.fiap3esr.autoescola3esr.application.core.specification.cancelamento.ValidadorInstrucaoJaCancelada;
import br.com.fiap3esr.autoescola3esr.application.port.out.InstrucaoRepository;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.MotivoCancelamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Expectativa: Validadores RN-C3 e RN-C4 controlam o cancelamento de instruções.")
class CancelamentoInstrucaoTest {
    @Mock
    InstrucaoRepository repository;

    ValidadorAntecedenciaCancelamento antecedencia;
    ValidadorInstrucaoJaCancelada jaCancelada;

    private final DadosCancelamento dados = new DadosCancelamento(1L, MotivoCancelamento.ALUNO_DESISTIU);

    @BeforeEach
    void setUp() {
        antecedencia = new ValidadorAntecedenciaCancelamento(repository);
        jaCancelada = new ValidadorInstrucaoJaCancelada(repository);
    }

    private void instrucaoEm(LocalDateTime dataHora, MotivoCancelamento motivo) {
        Instrucao instrucao = new Instrucao(1L, null, null, dataHora, motivo);
        when(repository.getReferenceById(1L)).thenReturn(instrucao);
    }

    @Test
    @DisplayName("Expectativa: RN-C3 rejeita cancelamento com menos de 24 horas de antecedência.")
    void antecedenciaCenario1() {
        instrucaoEm(LocalDateTime.now().plusHours(23), null);

        assertThatThrownBy(() -> antecedencia.validar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Instrução somente pode ser cancelada com antecedência mínima de 24 horas!");
    }

    @Test
    @DisplayName("Expectativa: RN-C3 rejeita cancelamento de instrução que já passou.")
    void antecedenciaCenario2() {
        instrucaoEm(LocalDateTime.now().minusDays(1), null);

        assertThatThrownBy(() -> antecedencia.validar(dados)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("Expectativa: RN-C3 aceita cancelamento com mais de 24 horas de antecedência.")
    void antecedenciaCenario3() {
        instrucaoEm(LocalDateTime.now().plusHours(25), null);

        assertThatCode(() -> antecedencia.validar(dados)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Expectativa: RN-C4 rejeita cancelar instrução já cancelada.")
    void jaCanceladaCenario1() {
        instrucaoEm(LocalDateTime.now().plusDays(3), MotivoCancelamento.OUTROS);

        assertThatThrownBy(() -> jaCancelada.validar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Instrução já está cancelada!");
    }

    @Test
    @DisplayName("Expectativa: RN-C4 aceita cancelar instrução ainda agendada.")
    void jaCanceladaCenario2() {
        instrucaoEm(LocalDateTime.now().plusDays(3), null);

        assertThatCode(() -> jaCancelada.validar(dados)).doesNotThrowAnyException();
    }
}
