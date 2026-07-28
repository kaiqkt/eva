package dev.kaiqkt.eva.adapter.outbound.forgejo.client

import dev.kaiqkt.eva.adapter.outbound.forgejo.client.dto.CreateRepositoryRequest
import dev.kaiqkt.eva.adapter.outbound.forgejo.client.dto.CreateRepositoryResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ForgejoHttpClient(
    @field:Qualifier("forgejo-rest-client")
    private val restClient: RestClient
) {
    fun createRepository(request: CreateRepositoryRequest): CreateRepositoryResponse {
        return restClient.post()
            .uri("/api/v1/user/repos")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus(HttpStatusCode::isError) { _, response ->
                val body = String(response.body.readAllBytes())
                throw ForgejoException("create repository failed: ${response.statusCode} $body")
            }
            .body(CreateRepositoryResponse::class.java)
            ?: throw ForgejoException("empty response from forgejo create repository")
    }
}
