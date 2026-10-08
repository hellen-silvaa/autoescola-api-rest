package br.com.fiap3esr.autoescola3esr.domain.usuario;

import br.com.fiap3esr.autoescola3esr.application.port.out.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Expectativa: A migration V10 cria o usuário administrador padrão.")
class UsuarioRepositoryTest {
    @Autowired
    UsuarioRepository repository;

    @Test
    @DisplayName("Expectativa: O seed admin existe, tem perfil ADMIN e a senha 'admin' confere com o hash BCrypt.")
    void seedAdminCenario1() {
        //act
        UserDetails admin = repository.findByLogin("admin");

        //assert
        assertThat(admin).isNotNull();
        assertThat(admin.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMIN");
        assertThat(new BCryptPasswordEncoder().matches("admin", admin.getPassword())).isTrue();
        assertThat(repository.existsByLogin("admin")).isTrue();
    }
}
