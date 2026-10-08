package br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao;

import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.Especialidade;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DadosAgendamento(
        @NotNull
        @JsonProperty("id_aluno")
        //@JsonAlias("id_aluno") //Anotação alternativa ao @JsonProperty
        Long idAluno,

        @JsonProperty("id_instrutor")
        Long idInstrutor,
        Especialidade especialidade,

        @NotNull
        @Future
        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm")
        @JsonProperty("data_hora")
        LocalDateTime dataHora) {
}