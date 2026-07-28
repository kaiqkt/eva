package dev.kaiqkt.eva.adapter.outbound.forgejo.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClient
import java.util.Base64

@Configuration
class ForgejoConfig {
    @Bean
    @Qualifier("forgejo-rest-client")
    fun forgejoRestClient(properties: ForgejoProperties): RestClient {
        val requestFactory = ClientHttpRequestFactoryBuilder.detect().build(
            ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(properties.connectTimeout)
                .withReadTimeout(properties.readTimeout)
        )

        return RestClient.builder()
            .baseUrl(properties.baseUrl)
            .requestFactory(requestFactory)
            .defaultHeader(
                HttpHeaders.AUTHORIZATION,
                "Basic ${Base64.getEncoder().encodeToString("${properties.user}:${properties.password}".toByteArray())}"
            )
            .build()
    }
}
