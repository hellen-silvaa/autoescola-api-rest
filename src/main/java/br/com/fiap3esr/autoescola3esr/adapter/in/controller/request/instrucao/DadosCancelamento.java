package br.com.fiap3esr.autoescola3esr.adapter.in.controller.request.instrucao;

import br.com.fiap3esr.autoescola3esr.shared.vo.enumeration.MotivoCancelamento;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record DadosCancelamento(
        @NotNull
        @JsonProperty("id_instrucao")
        Long idInstrucao,

        @NotNull
        MotivoCancelamento motivo) {
}
