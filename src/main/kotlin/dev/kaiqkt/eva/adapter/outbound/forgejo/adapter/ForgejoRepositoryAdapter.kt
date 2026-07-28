package dev.kaiqkt.eva.adapter.outbound.forgejo.adapter

import dev.kaiqkt.eva.adapter.outbound.forgejo.client.ForgejoHttpClient
import dev.kaiqkt.eva.adapter.outbound.forgejo.client.dto.CreateRepositoryRequest
import dev.kaiqkt.eva.application.port.outbound.GitRepositoryPort
import dev.kaiqkt.eva.domain.model.Repository
import org.springframework.stereotype.Component

@Component
class ForgejoRepositoryAdapter(
    private val httpClient: ForgejoHttpClient
) : GitRepositoryPort {
    override fun create(name: String, description: String?): Repository {
        val request = CreateRepositoryRequest(name = name, description = description)
        val response = httpClient.createRepository(request)

        return Repository(url = response.htmlUrl)
    }
}
