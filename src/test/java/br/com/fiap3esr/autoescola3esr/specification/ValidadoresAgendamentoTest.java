package br.com.fiap3esr.autoescola3esr.specification;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3esr.autoescola3esr.application.core.specification.ValidadorHoraInteira;
import br.com.fiap3esr.autoescola3esr.application.core.specification.ValidadorHorarioAntecedencia;
import br.com.fiap3esr.autoescola3esr.application.core.specification.ValidadorHorarioFuncionamento;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Expectativa: Validadores RN-A1, RN-A2 e RN-A3 aceitam casos válidos e rejeitam inválidos.")
class ValidadoresAgendamentoTest {
    private final ValidadorHorarioFuncionamento funcionamento = new ValidadorHorarioFuncionamento();
    private final ValidadorHorarioAntecedencia antecedencia = new ValidadorHorarioAntecedencia();
    private final ValidadorHoraInteira horaInteira = new ValidadorHoraInteira();

    private static LocalDateTime proximo(DayOfWeek dia, int hora, int minuto) {
        return LocalDateTime.now()
                .with(TemporalAdjusters.next(dia))
                .withHour(hora).withMinute(minuto).withSecond(0).withNano(0);
    }

    private static DadosAgendamento dados(LocalDateTime dataHora) {
        return new DadosAgendamento(1L, 1L, null, dataHora);
    }

    @Test
    @DisplayName("Expectativa: RN-A1 aceita segunda-feira às 10:00.")
    void horarioFuncionamentoCenario1() {
        assertThatCode(() -> funcionamento.validar(dados(proximo(DayOfWeek.MONDAY, 10, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Expectativa: RN-A1 aceita sábado às 20:00 (última aula).")
    void horarioFuncionamentoCenario2() {
        assertThatCode(() -> funcionamento.validar(dados(proximo(DayOfWeek.SATURDAY, 20, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Expectativa: RN-A1 rejeita domingo.")
    void horarioFuncionamentoCenario3() {
        assertThatThrownBy(() -> funcionamento.validar(dados(proximo(DayOfWeek.SUNDAY, 10, 0))))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Tentativa de agendamento fora do horário de funcionamento!");
    }

    @Test
    @DisplayName("Expectativa: RN-A1 rejeita antes das 06:00 e a partir das 21:00.")
    void horarioFuncionamentoCenario4() {
        assertThatThrownBy(() -> funcionamento.validar(dados(proximo(DayOfWeek.MONDAY, 5, 0))))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> funcionamento.validar(dados(proximo(DayOfWeek.MONDAY, 21, 0))))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("Expectativa: RN-A2 aceita agendamento com 1 dia de antecedência.")
    void antecedenciaCenario1() {
        assertThatCode(() -> antecedencia.validar(dados(LocalDateTime.now().plusDays(1))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Expectativa: RN-A2 rejeita agendamento com menos de 30 minutos de antecedência.")
    void antecedenciaCenario2() {
        assertThatThrownBy(() -> antecedencia.validar(dados(LocalDateTime.now().plusMinutes(10))))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("O agendamento precisa ocorrer com antecedência mínima de 30 minutos!");
    }

    @Test
    @DisplayName("Expectativa: RN-A3 aceita hora inteira.")
    void horaInteiraCenario1() {
        assertThatCode(() -> horaInteira.validar(dados(proximo(DayOfWeek.MONDAY, 9, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Expectativa: RN-A3 rejeita horário quebrado.")
    void horaInteiraCenario2() {
        assertThatThrownBy(() -> horaInteira.validar(dados(proximo(DayOfWeek.MONDAY, 9, 30))))
                .isInstanceOf(ValidacaoException.class);
    }
}
