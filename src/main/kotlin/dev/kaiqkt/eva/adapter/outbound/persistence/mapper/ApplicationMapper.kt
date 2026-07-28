package dev.kaiqkt.eva.adapter.outbound.persistence.mapper

import dev.kaiqkt.eva.adapter.outbound.persistence.entity.ApplicationEntity
import dev.kaiqkt.eva.adapter.outbound.persistence.entity.RepositoryEntity
import dev.kaiqkt.eva.domain.model.Application
import dev.kaiqkt.eva.domain.model.Repository

object ApplicationMapper {
    fun Application.toEntity(): ApplicationEntity {
        return ApplicationEntity(
            name = this.name,
            slug = this.slug,
            description = this.description,
            repository = RepositoryEntity(
                url = this.repository.url,
            ),
        )
    }

    fun ApplicationEntity.toDomain(): Application {
        return Application(
            name = this.name,
            slug = this.slug,
            description = this.description,
            repository = Repository(
                url = this.repository.url,
            ),
        )
    }
}
