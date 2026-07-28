package dev.kaiqkt.eva.adapter.outbound.persistence.adapter

import dev.kaiqkt.eva.adapter.outbound.persistence.mapper.ProjectMapper.toDomain
import dev.kaiqkt.eva.adapter.outbound.persistence.mapper.ProjectMapper.toEntity
import dev.kaiqkt.eva.adapter.outbound.persistence.repository.ProjectJpaRepository
import dev.kaiqkt.eva.application.port.outbound.ProjectRepositoryPort
import dev.kaiqkt.eva.domain.model.Project
import org.springframework.stereotype.Component

@Component
class ProjectRepositoryAdapter(
    private val projectJpaRepository: ProjectJpaRepository
) : ProjectRepositoryPort {
    override fun save(project: Project): Project {
        return projectJpaRepository.save(project.toEntity()).toDomain()
    }

    override fun existsBySlug(slug: String): Boolean {
        return projectJpaRepository.existsBySlug(slug)
    }

    override fun findBySlug(slug: String): Project? {
        return projectJpaRepository.findBySlug(slug)?.toDomain()
    }
}
