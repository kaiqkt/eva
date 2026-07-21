package dev.kaiqkt.eva.application.usecase

import dev.kaiqkt.eva.application.port.inbound.CreateApplicationUseCase
import dev.kaiqkt.eva.application.port.outbound.ApplicationRepositoryPort
import dev.kaiqkt.eva.domain.exception.ApplicationAlreadyExistsException
import dev.kaiqkt.eva.domain.model.Application
import dev.kaiqkt.eva.domain.service.SlugGenerator
import org.springframework.stereotype.Service

@Service
class CreateApplicationService(
    private val applicationRepositoryPort: ApplicationRepositoryPort,
) : CreateApplicationUseCase {
    override fun create(name: String, description: String?): Application {
        val slug = SlugGenerator.fromName(name)

        if (applicationRepositoryPort.existsBySlug(slug)) {
            throw ApplicationAlreadyExistsException(slug)
        }

        val application = Application(
            name = name,
            slug = slug,
            description = description,
        )

        return applicationRepositoryPort.save(application)
    }
}
