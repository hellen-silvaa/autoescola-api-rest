package br.com.fiap3esr.autoescola3esr.domain.instrucao;

import br.com.fiap3esr.autoescola3esr.application.core.domain.Aluno;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrucao;
import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrutor;
import br.com.fiap3esr.autoescola3esr.application.port.out.InstrucaoRepository;
import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.Endereco;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Especialidade;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.MotivoCancelamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Expectativa: Consultas de RN-A6 e RN-A7 ignoram instruções canceladas.")
class InstrucaoRepositoryTest {
    @Autowired
    InstrucaoRepository repository;

    @Autowired
    TestEntityManager testEntity;

    private final LocalDateTime proximaSegundaAs10 = LocalDateTime
            .now()
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .withHour(10)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);

    @Test
    @DisplayName("Expectativa: Retornar true quando o instrutor tiver instrução ativa no horário (RN-A6).")
    void existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNullCenario1() {
        //arrange
        Aluno aluno = cadastrarAluno();
        Instrutor instrutor = cadastrarInstrutor();
        persistirInstrucao(aluno, instrutor, proximaSegundaAs10, null);

        //act
        boolean existe = repository.existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull(
                instrutor.getId(), proximaSegundaAs10);

        //assert
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("Expectativa: Retornar false quando a instrução do instrutor no horário estiver cancelada (RN-A6).")
    void existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNullCenario2() {
        //arrange
        Aluno aluno = cadastrarAluno();
        Instrutor instrutor = cadastrarInstrutor();
        persistirInstrucao(aluno, instrutor, proximaSegundaAs10, MotivoCancelamento.INSTRUTOR_CANCELOU);

        //act
        boolean existe = repository.existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull(
                instrutor.getId(), proximaSegundaAs10);

        //assert
        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("Expectativa: Retornar true quando o aluno tiver instrução ativa no dia (RN-A7).")
    void existsByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNullCenario1() {
        //arrange
        Aluno aluno = cadastrarAluno();
        Instrutor instrutor = cadastrarInstrutor();
        persistirInstrucao(aluno, instrutor, proximaSegundaAs10, null);

        //act
        boolean existe = repository.existsByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(
                aluno.getId(), proximaSegundaAs10.withHour(6), proximaSegundaAs10.withHour(21));

        //assert
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("Expectativa: Retornar false quando a instrução do aluno no dia estiver cancelada (RN-A7).")
    void existsByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNullCenario2() {
        //arrange
        Aluno aluno = cadastrarAluno();
        Instrutor instrutor = cadastrarInstrutor();
        persistirInstrucao(aluno, instrutor, proximaSegundaAs10, MotivoCancelamento.ALUNO_DESISTIU);

        //act
        boolean existe = repository.existsByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(
                aluno.getId(), proximaSegundaAs10.withHour(6), proximaSegundaAs10.withHour(21));

        //assert
        assertThat(existe).isFalse();
    }

    private Endereco dadosEndereco() {
        return new Endereco(
                "Rua Teste",
                "000",
                "Apto. 00",
                "Vila Teste",
                "Test City",
                "TS",
                "01234-567"
        );
    }

    private Aluno cadastrarAluno() {
        Aluno aluno = new Aluno(
                null,
                "Aluno Teste",
                "alunoteste@email.com.br",
                "(11) 98765-4321",
                "12345678901",
                dadosEndereco(),
                true
        );
        testEntity.persist(aluno);
        return aluno;
    }

    private Instrutor cadastrarInstrutor() {
        Instrutor instrutor = new Instrutor(
                null,
                "Instrutor Teste",
                "instrutorteste@email.com.br",
                "(11) 91234-5678",
                "01234567890",
                Especialidade.MOTOS,
                dadosEndereco(),
                true
        );
        testEntity.persist(instrutor);
        return instrutor;
    }

    private void persistirInstrucao(Aluno aluno, Instrutor instrutor, LocalDateTime dataHora,
                                    MotivoCancelamento motivo) {
        Instrucao instrucao = new Instrucao(null, aluno, instrutor, dataHora);
        if (motivo != null) {
            instrucao.cancelar(motivo);
        }
        testEntity.persist(instrucao);
        testEntity.flush();
    }
}
