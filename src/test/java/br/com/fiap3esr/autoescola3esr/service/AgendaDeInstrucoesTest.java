package br.com.fiap3esr.autoescola3esr.service;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosCancelamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Aluno;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrucao;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrutor;
import br.com.fiap3esr.autoescola3esr.application.core.specification.contract.ValidadorAgendamento;
import br.com.fiap3esr.autoescola3esr.application.core.specification.contract.ValidadorCancelamento;
import br.com.fiap3esr.autoescola3esr.application.port.out.AlunoRepository;
import br.com.fiap3esr.autoescola3esr.application.port.out.InstrucaoRepository;
import br.com.fiap3esr.autoescola3esr.application.port.out.InstrutorRepository;
import br.com.fiap3esr.autoescola3esr.application.service.AgendaDeInstrucoes;
import br.com.fiap3esr.autoescola3esr.exception.type.AlunoNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.InstrucaoNotFoundException;
import br.com.fiap3esr.autoescola3esr.exception.type.ValidacaoException;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Especialidade;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.MotivoCancelamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Expectativa: AgendaDeInstrucoes aplica as regras de agendamento e cancelamento.")
class AgendaDeInstrucoesTest {
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    InstrutorRepository instrutorRepository;
    @Mock
    InstrucaoRepository instrucaoRepository;
    @Mock
    ValidadorAgendamento validadorAgendamento;
    @Mock
    ValidadorCancelamento validadorCancelamento;

    AgendaDeInstrucoes agenda;

    private final LocalDateTime proximaSegundaAs10 = LocalDateTime.now()
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .withHour(10).withMinute(0).withSecond(0).withNano(0);

    @BeforeEach
    void setUp() {
        agenda = new AgendaDeInstrucoes(alunoRepository, instrutorRepository, instrucaoRepository,
                List.of(validadorAgendamento), List.of(validadorCancelamento));
    }

    private Aluno aluno() {
        return new Aluno(1L, "Aluno Teste", "aluno@email.com", "11999999999", "12345678901", null, true);
    }

    private Instrutor instrutor() {
        return new Instrutor(2L, "Instrutor Teste", "instrutor@email.com", "11988888888", "01234567890",
                Especialidade.MOTOS, null, true);
    }

    @Test
    @DisplayName("Expectativa: Lançar AlunoNotFoundException quando o aluno não existir.")
    void agendarCenario1() {
        //arrange
        var dados = new DadosAgendamento(1L, 2L, null, proximaSegundaAs10);
        when(alunoRepository.existsById(1L)).thenReturn(false);

        //act + assert
        assertThatThrownBy(() -> agenda.agendar(dados)).isInstanceOf(AlunoNotFoundException.class);
        verify(instrucaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: Lançar ValidacaoException sem instrutor e sem especialidade (RN-A8).")
    void agendarCenario2() {
        //arrange
        var dados = new DadosAgendamento(1L, null, null, proximaSegundaAs10);
        when(alunoRepository.existsById(1L)).thenReturn(true);

        //act + assert
        assertThatThrownBy(() -> agenda.agendar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("Especialidade");
        verify(instrucaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: Lançar ValidacaoException quando nenhum instrutor estiver livre (RN-A8).")
    void agendarCenario3() {
        //arrange
        var dados = new DadosAgendamento(1L, null, Especialidade.MOTOS, proximaSegundaAs10);
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(instrutorRepository.escolherInstrutorAleatorioDisponivel(Especialidade.MOTOS, proximaSegundaAs10))
                .thenReturn(null);

        //act + assert
        assertThatThrownBy(() -> agenda.agendar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Não existe instrutor disponível para a data/hora informada!");
        verify(instrucaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: Salvar a instrução e executar os validadores quando os dados forem válidos.")
    void agendarCenario4() {
        //arrange
        var dados = new DadosAgendamento(1L, null, Especialidade.MOTOS, proximaSegundaAs10);
        Aluno aluno = aluno();
        Instrutor instrutor = instrutor();
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(alunoRepository.getReferenceById(1L)).thenReturn(aluno);
        when(instrutorRepository.escolherInstrutorAleatorioDisponivel(Especialidade.MOTOS, proximaSegundaAs10))
                .thenReturn(instrutor);
        when(instrucaoRepository.save(any(Instrucao.class)))
                .thenReturn(new Instrucao(10L, aluno, instrutor, proximaSegundaAs10));

        //act
        DadosDetalhamentoAgendamento resultado = agenda.agendar(dados);

        //assert
        verify(validadorAgendamento).validar(dados);
        verify(instrucaoRepository).save(any(Instrucao.class));
        assertThat(resultado.id()).isEqualTo(10L);
        assertThat(resultado.nomeInstrutor()).isEqualTo("Instrutor Teste");
    }

    @Test
    @DisplayName("Expectativa: Lançar InstrucaoNotFoundException ao cancelar instrução inexistente (RN-C2).")
    void cancelarCenario1() {
        //arrange
        var dados = new DadosCancelamento(99L, MotivoCancelamento.OUTROS);
        when(instrucaoRepository.existsById(99L)).thenReturn(false);

        //act + assert
        assertThatThrownBy(() -> agenda.cancelar(dados))
                .isInstanceOf(InstrucaoNotFoundException.class)
                .hasMessage("ID da instrução informado não existe!");
        verifyNoInteractions(validadorCancelamento);
    }

    @Test
    @DisplayName("Expectativa: Registrar o motivo do cancelamento após executar os validadores.")
    void cancelarCenario2() {
        //arrange
        var dados = new DadosCancelamento(10L, MotivoCancelamento.ALUNO_DESISTIU);
        Instrucao instrucao = new Instrucao(10L, aluno(), instrutor(), proximaSegundaAs10);
        when(instrucaoRepository.existsById(10L)).thenReturn(true);
        when(instrucaoRepository.getReferenceById(10L)).thenReturn(instrucao);

        //act
        agenda.cancelar(dados);

        //assert
        verify(validadorCancelamento).validar(dados);
        assertThat(instrucao.isCancelada()).isTrue();
        assertThat(instrucao.getMotivoCancelamento()).isEqualTo(MotivoCancelamento.ALUNO_DESISTIU);
    }

    @Test
    @DisplayName("Expectativa: Detalhar instrução cancelada com status CANCELADA.")
    void detalharCenario1() {
        //arrange
        Instrucao instrucao = new Instrucao(10L, aluno(), instrutor(), proximaSegundaAs10);
        instrucao.cancelar(MotivoCancelamento.INSTRUTOR_CANCELOU);
        when(instrucaoRepository.findById(10L)).thenReturn(java.util.Optional.of(instrucao));

        //act
        var detalhe = agenda.detalhar(10L);

        //assert
        assertThat(detalhe.status()).isEqualTo("CANCELADA");
        assertThat(detalhe.motivoCancelamento()).isEqualTo(MotivoCancelamento.INSTRUTOR_CANCELOU);
    }
}
