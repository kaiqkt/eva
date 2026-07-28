package dev.kaiqkt.eva.application.usecase

import dev.kaiqkt.eva.application.port.inbound.CreateApplicationUseCase
import dev.kaiqkt.eva.application.port.outbound.ApplicationRepositoryPort
import dev.kaiqkt.eva.application.port.outbound.GitRepositoryPort
import dev.kaiqkt.eva.domain.exception.ApplicationAlreadyExistsException
import dev.kaiqkt.eva.domain.model.Application
import dev.kaiqkt.eva.domain.service.SlugGenerator
import org.springframework.stereotype.Service

@Service
class CreateApplicationService(
    private val gitRepository: GitRepositoryPort,
    private val applicationRepository: ApplicationRepositoryPort
) : CreateApplicationUseCase {
    override fun create(name: String, description: String?): Application {
        val slug = SlugGenerator.fromName(name)

        if (applicationRepository.existsBySlug(slug)) {
            throw ApplicationAlreadyExistsException()
        }

        val repository = gitRepository.create(slug, description)

        val application = Application(
            name = name,
            slug = slug,
            description = description,
            repository = repository
        )

        return applicationRepository.save(application)
    }
}
