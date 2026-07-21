package dev.kaiqkt.eva.adapter.outbound.persistence.mapper

import dev.kaiqkt.eva.adapter.outbound.persistence.entity.ApplicationEntity
import dev.kaiqkt.eva.domain.model.Application

object ApplicationMapper {
    fun Application.toEntity(): ApplicationEntity {
        return ApplicationEntity(
            name = this.name,
            slug = this.slug,
            description = this.description,
        )
    }

    fun ApplicationEntity.toDomain(): Application {
        return Application(
            id = this.id,
            name = this.name,
            slug = this.slug,
            description = this.description,
        )
    }
}
