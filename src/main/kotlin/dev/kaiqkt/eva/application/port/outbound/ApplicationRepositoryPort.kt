package dev.kaiqkt.eva.application.port.outbound

import dev.kaiqkt.eva.domain.model.Application

interface ApplicationRepositoryPort {
    fun save(application: Application): Application
    fun existsBySlug(slug: String): Boolean
}
