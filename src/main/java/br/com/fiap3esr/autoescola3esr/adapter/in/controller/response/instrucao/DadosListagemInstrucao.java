package br.com.fiap3esr.autoescola3esr.adapter.in.controller.response.instrucao;

import br.com.fiap3esr.autoescola3esr.application.core.domain.Instrucao;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record DadosListagemInstrucao(
        Long id,

        @JsonProperty("nome_aluno")
        String nomeAluno,

        @JsonProperty("nome_instrutor")
        String nomeInstrutor,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm")
        @JsonProperty("data_hora")
        LocalDateTime dataHora,

        String status) {
    public DadosListagemInstrucao(Instrucao instrucao) {
        this(
                instrucao.getId(),
                instrucao.getAluno().getNome(),
                instrucao.getInstrutor().getNome(),
                instrucao.getDataHora(),
                instrucao.isCancelada() ? "CANCELADA" : "AGENDADA"
        );
    }
}
