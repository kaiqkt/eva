package dev.kaiqkt.eva.application.port.outbound

import dev.kaiqkt.eva.domain.model.Project

interface ProjectRepositoryPort {
    fun save(project: Project): Project
    fun existsBySlug(slug: String): Boolean
    fun findBySlug(slug: String): Project?
}
