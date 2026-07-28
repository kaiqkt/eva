package dev.kaiqkt.eva.application.usecase

import dev.kaiqkt.eva.application.port.inbound.CreateProjectUseCase
import dev.kaiqkt.eva.application.port.outbound.ProjectRepositoryPort
import dev.kaiqkt.eva.domain.exception.ResourceAlreadyExistsException
import dev.kaiqkt.eva.domain.model.Project
import dev.kaiqkt.eva.domain.service.SlugGenerator
import org.springframework.stereotype.Service

@Service
class CreateProjectService(
    private val projectRepositoryPort: ProjectRepositoryPort
) : CreateProjectUseCase {
    override fun create(name: String, description: String?): Project {
        val slug = SlugGenerator.fromName(name)

        if (projectRepositoryPort.existsBySlug(slug)) {
            throw ResourceAlreadyExistsException("Project")
        }

        val project = Project(
            name = name,
            slug = slug,
            description = description
        )

        return projectRepositoryPort.save(project)
    }
}
