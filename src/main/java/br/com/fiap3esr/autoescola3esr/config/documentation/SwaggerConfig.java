package br.com.fiap3esr.autoescola3esr.config.documentation;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                "bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .info(new Info()
                        .title("Auto Escola 3ESR")
                        .version("1.0")
                        .description("""
                                API REST para gerenciamento de instruções da auto escola 3ESR.

                                **Integrantes**
                                - Alexia Ramalho — RM 558385
                                - Enzo Real — RM 557943
                                - Gustavo Pasquini — RM 555454
                                - Hellen Silva — RM 559008
                                - Lorenzo Acquesta — RM 557397
                                """)
                        .contact(new Contact()
                                .name("Auto Escola 3ESR")
                                .email("autoescola3esr@email.com.br")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")
                        )
                );
    }
}