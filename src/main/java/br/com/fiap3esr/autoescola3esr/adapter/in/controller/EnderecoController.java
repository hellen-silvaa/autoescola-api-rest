package br.com.fiap3esr.autoescola3esr.adapter.in.controller;

import br.com.fiap3esr.autoescola3esr.application.service.EnderecoService;
import br.com.fiap3esr.autoescola3esr.shared.vo.endereco.dto.DadosEndereco;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enderecos")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Endereços", description = "Consulta de endereços por CEP via WebService externo ViaCEP")
@RequiredArgsConstructor
public class EnderecoController {
    private final EnderecoService service;

    @GetMapping("/{cep}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Consultar endereço por CEP",
            description = "Consulta o endereço no ViaCEP. Aceita o CEP com ou sem hífen (ex.: 01001-000 ou 01001000).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
            @ApiResponse(responseCode = "400", description = "CEP em formato inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "CEP não encontrado"),
            @ApiResponse(responseCode = "503", description = "Serviço de CEP indisponível")})
    public ResponseEntity<DadosEndereco> consultar(
            @Parameter(description = "CEP com 8 dígitos, com ou sem hífen", example = "01001-000")
            @PathVariable String cep) {
        return ResponseEntity.ok(service.consultarPorCep(cep));
    }
}
