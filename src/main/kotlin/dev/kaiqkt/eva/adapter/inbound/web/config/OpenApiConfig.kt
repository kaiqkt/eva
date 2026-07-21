package dev.kaiqkt.eva.adapter.inbound.web.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Sem este bean o springdoc sobe com título/versão genéricos ("OpenAPI
 * definition"). O bean só nomeia a API — o schema em si é inferido dos
 * controllers e enriquecido com anotações por endpoint.
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun evaOpenApi(): OpenAPI =
        OpenAPI().info(
            Info()
                .title("eva-server")
                .version("v1")
                .description("API do eva"),
        )
}
