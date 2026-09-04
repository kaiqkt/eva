package dev.kaiqkt.eva.adapter.outbound.forgejo.client

import dev.kaiqkt.eva.adapter.outbound.forgejo.client.request.CreateRepositoryRequest
import dev.kaiqkt.eva.adapter.outbound.forgejo.client.response.CreateRepositoryResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

private const val ERROR_BODY_LOG_LIMIT = 2048

@Component
class ForgejoHttpClient(
    @param:Qualifier("forgejo-rest-client")
    private val restClient: RestClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun createRepository(request: CreateRepositoryRequest): CreateRepositoryResponse {
        val response = try {
            restClient.post()
                .uri("/api/v1/user/repos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError) { _, response -> raiseFor("create repository", response) }
                .body(CreateRepositoryResponse::class.java)
        } catch (exception: RestClientException) {
            log.error("forgejo create repository request failed", exception)
            throw ForgejoException("create repository request failed", exception)
        }

        return response ?: throw ForgejoException("empty response from forgejo create repository")
    }

    private fun raiseFor(operation: String, response: ClientHttpResponse): Nothing {
        val body = response.body.readNBytes(ERROR_BODY_LOG_LIMIT).toString(Charsets.UTF_8)
        log.error("forgejo {} failed: status={} body={}", operation, response.statusCode, body)
        throw ForgejoException("$operation failed with status ${response.statusCode}")
    }
}
