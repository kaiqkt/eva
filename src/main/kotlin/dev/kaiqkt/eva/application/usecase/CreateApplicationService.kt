package dev.kaiqkt.eva.application.usecase

import dev.kaiqkt.eva.application.port.inbound.CreateApplicationUseCase
import dev.kaiqkt.eva.application.port.outbound.ApplicationRepositoryPort
import dev.kaiqkt.eva.application.port.outbound.GitPort
import dev.kaiqkt.eva.application.port.outbound.ProjectRepositoryPort
import dev.kaiqkt.eva.domain.exception.ResourceAlreadyExistsException
import dev.kaiqkt.eva.domain.exception.ResourceNotFoundException
import dev.kaiqkt.eva.domain.model.Application
import dev.kaiqkt.eva.domain.service.SlugGenerator
import org.springframework.stereotype.Service

@Service
class CreateApplicationService(
    private val git: GitPort,
    private val applicationRepository: ApplicationRepositoryPort,
    private val projectRepository: ProjectRepositoryPort
) : CreateApplicationUseCase {
    override fun create(projectSlug: String, name: String, description: String?): Application {
        val project = projectRepository.findBySlug(projectSlug)
            ?: throw ResourceNotFoundException("Project")

        val slug = SlugGenerator.fromName(name)

        if (applicationRepository.existsBySlug(slug)) {
            throw ResourceAlreadyExistsException("Application")
        }

        val repository = git.create(slug, description)

        val application = Application(
            name = name,
            slug = slug,
            description = description,
            projectId = requireNotNull(project.id),
            repository = repository
        )

        return applicationRepository.save(application)
    }
}
