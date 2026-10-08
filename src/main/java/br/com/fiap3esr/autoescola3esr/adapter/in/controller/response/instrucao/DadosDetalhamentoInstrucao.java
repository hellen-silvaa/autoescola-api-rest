package br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrucao;

import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrucao;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Especialidade;
import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.MotivoCancelamento;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record DadosDetalhamentoInstrucao(
        Long id,

        @JsonProperty("id_aluno")
        Long idAluno,

        @JsonProperty("nome_aluno")
        String nomeAluno,

        @JsonProperty("id_instrutor")
        Long idInstrutor,

        @JsonProperty("nome_instrutor")
        String nomeInstrutor,
        Especialidade especialidade,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm")
        @JsonProperty("data_hora")
        LocalDateTime dataHora,

        String status,

        @JsonProperty("motivo_cancelamento")
        MotivoCancelamento motivoCancelamento) {
    public DadosDetalhamentoInstrucao(Instrucao instrucao) {
        this(
                instrucao.getId(),
                instrucao.getAluno().getId(),
                instrucao.getAluno().getNome(),
                instrucao.getInstrutor().getId(),
                instrucao.getInstrutor().getNome(),
                instrucao.getInstrutor().getEspecialidade(),
                instrucao.getDataHora(),
                instrucao.isCancelada() ? "CANCELADA" : "AGENDADA",
                instrucao.getMotivoCancelamento()
        );
    }
}
