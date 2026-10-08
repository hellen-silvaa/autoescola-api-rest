package br.com.fiap3esr.autoescola3esr.domain.aluno;

import br.com.fiap3esr.autoescola3esr.application.core.domain.Aluno;
import br.com.fiap3esr.autoescola3esr.application.port.out.AlunoRepository;
import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Expectativa: AlunoRepository filtra alunos por status ativo.")
public class AlunoRepositoryTest {
    @Autowired
    AlunoRepository repository;

    @Autowired
    TestEntityManager testEntity;

    @Test
    @DisplayName("Expectativa: findAllByAtivoTrue retorna apenas alunos ativos.")
    void findAllByAtivoTrueCenario1() {
        //arrange
        Aluno ativo = cadastrarAluno("Aluno Ativo", "11111111111", true);
        cadastrarAluno("Aluno Inativo", "22222222222", false);

        //act
        Page<Aluno> pagina = repository.findAllByAtivoTrue(PageRequest.of(0, 10));

        //assert
        assertThat(pagina.getContent()).containsExactly(ativo);
    }

    @Test
    @DisplayName("Expectativa: existsByIdAndAtivoFalse retorna true para aluno inativo.")
    void existsByIdAndAtivoFalseCenario1() {
        //arrange
        Aluno inativo = cadastrarAluno("Aluno Inativo", "22222222222", false);

        //act
        boolean resultado = repository.existsByIdAndAtivoFalse(inativo.getId());

        //assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Expectativa: existsByIdAndAtivoFalse retorna false para aluno ativo.")
    void existsByIdAndAtivoFalseCenario2() {
        //arrange
        Aluno ativo = cadastrarAluno("Aluno Ativo", "11111111111", true);

        //act
        boolean resultado = repository.existsByIdAndAtivoFalse(ativo.getId());

        //assert
        assertThat(resultado).isFalse();
    }

    private Aluno cadastrarAluno(String nome, String cpf, boolean ativo) {
        Aluno aluno = new Aluno(
                null,
                nome,
                nome.toLowerCase().replace(" ", ".") + "@email.com.br",
                "(11) 98765-4321",
                cpf,
                new Endereco("Rua Teste", "000", "Apto. 00", "Vila Teste", "Test City", "TS", "01234-567"),
                ativo
        );
        testEntity.persist(aluno);
        return aluno;
    }
}
