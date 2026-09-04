package dev.kaiqkt.eva.application.port.outbound

import dev.kaiqkt.eva.domain.model.GitRepository

interface CodeHostingPort {
    fun create(slug: String, description: String?): GitRepository
}
