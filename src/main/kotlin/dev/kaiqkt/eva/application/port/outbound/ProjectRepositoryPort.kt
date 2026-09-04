package dev.kaiqkt.eva.application.port.outbound

import dev.kaiqkt.eva.domain.model.Project
import dev.kaiqkt.eva.domain.model.Slug

interface ProjectRepositoryPort {
    fun save(project: Project): Project
    fun existsBySlug(slug: Slug): Boolean
    fun findBySlug(slug: Slug): Project?
}
