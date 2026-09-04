package dev.kaiqkt.eva.adapter.outbound.persistence

import dev.kaiqkt.eva.adapter.outbound.persistence.jpa.ApplicationJpaRepository
import dev.kaiqkt.eva.adapter.outbound.persistence.mapper.ApplicationMapper.toDomain
import dev.kaiqkt.eva.adapter.outbound.persistence.mapper.ApplicationMapper.toEntity
import dev.kaiqkt.eva.application.port.outbound.ApplicationRepositoryPort
import dev.kaiqkt.eva.domain.model.Application
import dev.kaiqkt.eva.domain.model.Slug
import org.springframework.stereotype.Component

@Component
class ApplicationPersistenceAdapter(
    private val applicationJpaRepository: ApplicationJpaRepository
) : ApplicationRepositoryPort {
    override fun save(application: Application): Application {
        return applicationJpaRepository.save(application.toEntity()).toDomain()
    }

    override fun existsBySlug(slug: Slug): Boolean {
        return applicationJpaRepository.existsBySlug(slug.value)
    }
}
