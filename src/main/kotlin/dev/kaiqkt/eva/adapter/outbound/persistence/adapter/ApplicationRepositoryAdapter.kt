package dev.kaiqkt.eva.adapter.outbound.persistence.adapter

import dev.kaiqkt.eva.adapter.outbound.persistence.mapper.ApplicationMapper.toDomain
import dev.kaiqkt.eva.adapter.outbound.persistence.mapper.ApplicationMapper.toEntity
import dev.kaiqkt.eva.adapter.outbound.persistence.repository.ApplicationJpaRepository
import dev.kaiqkt.eva.application.port.outbound.ApplicationRepositoryPort
import dev.kaiqkt.eva.domain.model.Application
import org.springframework.stereotype.Component

@Component
class ApplicationRepositoryAdapter(
    private val applicationJpaRepository: ApplicationJpaRepository,
) : ApplicationRepositoryPort {
    override fun save(application: Application): Application {
        return applicationJpaRepository.save(application.toEntity()).toDomain()
    }

    override fun existsBySlug(slug: String): Boolean {
        return applicationJpaRepository.existsBySlug(slug)
    }
}
