package dev.kaiqkt.eva.application.usecase

import dev.kaiqkt.eva.application.port.inbound.CreateProjectUseCase
import dev.kaiqkt.eva.application.port.outbound.ProjectRepositoryPort
import dev.kaiqkt.eva.domain.exception.ResourceAlreadyExistsException
import dev.kaiqkt.eva.domain.model.Project
import org.springframework.stereotype.Service

@Service
class CreateProjectUseCaseImpl(
    private val projectRepository: ProjectRepositoryPort
) : CreateProjectUseCase {
    override fun create(name: String, description: String?): Project {
        val project = Project.create(name, description)

        if (projectRepository.existsBySlug(project.slug)) {
            throw ResourceAlreadyExistsException("Project")
        }

        return projectRepository.save(project)
    }
}
