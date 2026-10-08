package br.com.fiap3esr.autoescola3esr.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Expectativa: CORS liberado apenas para as origens configuradas em api.cors.allowed-origins.")
class CorsConfigTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Expectativa: Retornar o header Access-Control-Allow-Origin para uma origem permitida.")
    void preflightCenario1() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(options("/instrutores")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo("http://localhost:3000");
        assertThat(response.getHeader("Access-Control-Allow-Credentials")).isEqualTo("true");
        assertThat(response.getHeader("Access-Control-Max-Age")).isEqualTo("3600");
    }

    @Test
    @DisplayName("Expectativa: Retornar código 403 para uma origem não permitida.")
    void preflightCenario2() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(options("/instrutores")
                        .header("Origin", "http://origem-maliciosa.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isNull();
    }
}
