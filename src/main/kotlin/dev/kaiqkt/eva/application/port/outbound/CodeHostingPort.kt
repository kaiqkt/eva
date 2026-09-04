package dev.kaiqkt.eva.application.port.outbound

import dev.kaiqkt.eva.domain.model.GitRepository
import dev.kaiqkt.eva.domain.model.Slug

interface CodeHostingPort {
    fun create(slug: Slug, description: String?): GitRepository
}
