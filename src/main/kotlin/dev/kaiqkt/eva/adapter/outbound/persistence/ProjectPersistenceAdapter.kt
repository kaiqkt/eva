package dev.kaiqkt.eva.adapter.outbound.persistence

import dev.kaiqkt.eva.adapter.outbound.persistence.jpa.ProjectJpaRepository
import dev.kaiqkt.eva.adapter.outbound.persistence.mapper.ProjectMapper.toDomain
import dev.kaiqkt.eva.adapter.outbound.persistence.mapper.ProjectMapper.toEntity
import dev.kaiqkt.eva.application.port.outbound.ProjectRepositoryPort
import dev.kaiqkt.eva.domain.model.Project
import dev.kaiqkt.eva.domain.model.Slug
import org.springframework.stereotype.Component

@Component
class ProjectPersistenceAdapter(
    private val projectJpaRepository: ProjectJpaRepository
) : ProjectRepositoryPort {
    override fun save(project: Project): Project {
        return projectJpaRepository.save(project.toEntity()).toDomain()
    }

    override fun existsBySlug(slug: Slug): Boolean {
        return projectJpaRepository.existsBySlug(slug.value)
    }

    override fun findBySlug(slug: Slug): Project? {
        return projectJpaRepository.findBySlug(slug.value)?.toDomain()
    }
}
