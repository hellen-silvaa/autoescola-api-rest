package br.com.fiap3esr.autoescola3esr.application.port.out;

import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrucao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InstrucaoRepository extends JpaRepository<Instrucao, Long> {
    boolean existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull(Long idInstrutor, LocalDateTime dataHora);

    boolean existsByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(
            Long idAluno, LocalDateTime inicioExpediente, LocalDateTime fimExpediente);

    Page<Instrucao> findAllByMotivoCancelamentoIsNull(Pageable paginacao);
}
