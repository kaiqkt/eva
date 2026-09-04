package dev.kaiqkt.eva.adapter.outbound.persistence.mapper

import dev.kaiqkt.eva.adapter.outbound.persistence.entity.ApplicationEntity
import dev.kaiqkt.eva.adapter.outbound.persistence.entity.GitRepositoryEntity
import dev.kaiqkt.eva.domain.model.Application
import dev.kaiqkt.eva.domain.model.GitRepository
import dev.kaiqkt.eva.domain.model.Slug

object ApplicationMapper {
    fun Application.toEntity(): ApplicationEntity {
        return ApplicationEntity(
            id = this.id,
            name = this.name,
            slug = this.slug.value,
            description = this.description,
            projectId = this.projectId,
            repository = GitRepositoryEntity(
                url = this.repository.url
            )
        )
    }

    fun ApplicationEntity.toDomain(): Application {
        return Application(
            id = this.id,
            name = this.name,
            slug = Slug.of(this.slug),
            description = this.description,
            projectId = this.projectId,
            repository = GitRepository(
                url = this.repository.url
            )
        )
    }
}
