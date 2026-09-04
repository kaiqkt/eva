package dev.kaiqkt.eva.application.port.outbound

import dev.kaiqkt.eva.domain.model.Application
import dev.kaiqkt.eva.domain.model.Slug

interface ApplicationRepositoryPort {
    fun save(application: Application): Application
    fun existsBySlug(slug: Slug): Boolean
}
