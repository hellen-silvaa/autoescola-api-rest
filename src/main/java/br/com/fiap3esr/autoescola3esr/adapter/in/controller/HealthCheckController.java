package br.com.fiap3esr.autoescola3esr.adapter.in.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-check")
@Tag(name = "Health Check", description = "Verificação de disponibilidade da API")
public class HealthCheckController {
    @GetMapping
    @Operation(summary = "Verificar se a API está no ar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "API disponível")})
    public String healthCheck() {
        return "Verificação de integridado da API da Auto Escola 3ESR!";
    }
}