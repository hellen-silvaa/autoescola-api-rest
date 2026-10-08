package br.com.fiap3esr.autoescola3esr.application.port.out;

import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrutor;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Especialidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface InstrutorRepository extends JpaRepository<Instrutor, Long> {
    Page<Instrutor> findAllByAtivoTrue(Pageable paginacao);

    @Query("""
        select i from Instrutor i
        where
        i.ativo = true
        and
        i.especialidade = :especialidade
        and
        i.id not in(
            select a.instrutor.id from Instrucao a
            where
            a.dataHora = :dataHora
            and a.motivoCancelamento is null
        )
        order by rand()
        limit 1
    """)
    Instrutor escolherInstrutorAleatorioDisponivel(Especialidade especialidade, LocalDateTime dataHora);

    boolean existsByIdAndAtivoFalse(Long id);
}
