package br.com.fiap3esr.autoescola3esr.controller;

import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao.DadosCancelamento;
import br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3esr.autoescola3esr.application.service.AgendaDeInstrucoes;
import br.com.fiap3esr.autoescola3esr.exception.type.InstrucaoNotFoundException;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Especialidade;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.MotivoCancelamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InstrucaoControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AgendaDeInstrucoes agenda;

    private static LocalDateTime proximaSegundaAs10() {
        return LocalDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    @DisplayName("Expectativa: Retornar código 400 para dados inválidos.")
    @WithMockUser
    void agendarInstrucaoCenario1() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/instrucoes"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Expectativa: Retornar código 200 e o detalhamento para um agendamento válido.")
    @WithMockUser
    void agendarInstrucaoCenario2() throws Exception {
        //arrange
        LocalDateTime dataHora = proximaSegundaAs10();
        String dataFormatada = dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
        when(agenda.agendar(any(DadosAgendamento.class))).thenReturn(
                new DadosDetalhamentoAgendamento(1L, "Aluno Teste", "Instrutor Teste", Especialidade.MOTOS, dataHora));
        String json = """
                {"id_aluno": 1, "id_instrutor": 2, "data_hora": "%s"}
                """.formatted(dataFormatada);

        //act
        MockHttpServletResponse response = mockMvc
                .perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .contains("\"nome_aluno\":\"Aluno Teste\"")
                .contains(dataFormatada);
        verify(agenda).agendar(any(DadosAgendamento.class));
    }

    @Test
    @DisplayName("Expectativa: Retornar código 400 ao cancelar sem motivo (RN-C1).")
    @WithMockUser
    void cancelarInstrucaoCenario1() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(delete("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id_instrucao": 1}
                                """))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verifyNoInteractions(agenda);
    }

    @Test
    @DisplayName("Expectativa: Retornar código 204 ao cancelar com dados válidos.")
    @WithMockUser(roles = "ADMIN")
    void cancelarInstrucaoCenario2() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(delete("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id_instrucao": 1, "motivo": "ALUNO_DESISTIU"}
                                """))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(agenda).cancelar(new DadosCancelamento(1L, MotivoCancelamento.ALUNO_DESISTIU));
    }

    @Test
    @DisplayName("Expectativa: Retornar código 401 ou 403 ao cancelar sem token.")
    void cancelarInstrucaoCenario3() throws Exception {
        //act
        MockHttpServletResponse response = mockMvc
                .perform(delete("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id_instrucao": 1, "motivo": "OUTROS"}
                                """))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
        verifyNoInteractions(agenda);
    }

    @Test
    @DisplayName("Expectativa: Retornar código 404 ao detalhar instrução inexistente (RN-C2).")
    @WithMockUser
    void detalharInstrucaoCenario1() throws Exception {
        //arrange
        when(agenda.detalhar(99L)).thenThrow(new InstrucaoNotFoundException("ID da instrução informado não existe!"));

        //act
        MockHttpServletResponse response = mockMvc
                .perform(get("/instrucoes/99"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Expectativa: Retornar código 200 na listagem e repassar o filtro ativas=true.")
    @WithMockUser
    void listarInstrucoesCenario1() throws Exception {
        //arrange
        when(agenda.listar(anyBoolean(), any())).thenReturn(Page.empty());

        //act
        MockHttpServletResponse response = mockMvc
                .perform(get("/instrucoes").param("ativas", "true"))
                .andReturn()
                .getResponse();

        //assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(agenda).listar(eq(true), any());
    }
}
