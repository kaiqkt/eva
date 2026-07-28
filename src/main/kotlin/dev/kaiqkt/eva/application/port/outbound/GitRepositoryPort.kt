package dev.kaiqkt.eva.application.port.outbound

import dev.kaiqkt.eva.domain.model.Repository

interface GitRepositoryPort {
    fun create(name: String, description: String?): Repository
}
