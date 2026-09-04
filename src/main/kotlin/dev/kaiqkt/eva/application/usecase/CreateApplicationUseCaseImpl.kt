package dev.kaiqkt.eva.application.usecase

import dev.kaiqkt.eva.application.port.inbound.CreateApplicationUseCase
import dev.kaiqkt.eva.application.port.outbound.ApplicationRepositoryPort
import dev.kaiqkt.eva.application.port.outbound.CodeHostingPort
import dev.kaiqkt.eva.application.port.outbound.ProjectRepositoryPort
import dev.kaiqkt.eva.domain.exception.ResourceAlreadyExistsException
import dev.kaiqkt.eva.domain.exception.ResourceNotFoundException
import dev.kaiqkt.eva.domain.model.Application
import dev.kaiqkt.eva.domain.model.Slug
import org.springframework.stereotype.Service

@Service
class CreateApplicationUseCaseImpl(
    private val codeHosting: CodeHostingPort,
    private val applicationRepository: ApplicationRepositoryPort,
    private val projectRepository: ProjectRepositoryPort
) : CreateApplicationUseCase {
    override fun create(projectSlug: String, name: String, description: String?): Application {
        val project = projectRepository.findBySlug(Slug.of(projectSlug))
            ?: throw ResourceNotFoundException("Project")

        val slug = Slug.fromName(name)

        if (applicationRepository.existsBySlug(slug)) {
            throw ResourceAlreadyExistsException("Application")
        }

        val repository = codeHosting.create(slug, description)

        return applicationRepository.save(
            Application.create(
                name = name,
                description = description,
                projectId = project.id,
                repository = repository
            )
        )
    }
}
