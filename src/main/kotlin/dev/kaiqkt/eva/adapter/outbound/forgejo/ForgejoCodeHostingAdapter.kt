package dev.kaiqkt.eva.adapter.outbound.forgejo

import dev.kaiqkt.eva.adapter.outbound.forgejo.client.ForgejoException
import dev.kaiqkt.eva.adapter.outbound.forgejo.client.ForgejoHttpClient
import dev.kaiqkt.eva.adapter.outbound.forgejo.client.request.CreateRepositoryRequest
import dev.kaiqkt.eva.application.port.outbound.CodeHostingException
import dev.kaiqkt.eva.application.port.outbound.CodeHostingPort
import dev.kaiqkt.eva.domain.model.GitRepository
import org.springframework.stereotype.Component

@Component
class ForgejoCodeHostingAdapter(
    private val httpClient: ForgejoHttpClient
) : CodeHostingPort {
    override fun create(slug: String, description: String?): GitRepository {
        val request = CreateRepositoryRequest(name = slug, description = description)

        val response = try {
            httpClient.createRepository(request)
        } catch (exception: ForgejoException) {
            throw CodeHostingException("failed to provision repository '$slug'", exception)
        }

        return GitRepository(url = response.htmlUrl)
    }
}
