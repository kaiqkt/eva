package dev.kaiqkt.eva.adapter.outbound.persistence.mapper

import dev.kaiqkt.eva.adapter.outbound.persistence.entity.ProjectEntity
import dev.kaiqkt.eva.domain.model.Project
import dev.kaiqkt.eva.domain.model.Slug

object ProjectMapper {
    fun Project.toEntity(): ProjectEntity {
        return ProjectEntity(
            id = this.id,
            name = this.name,
            slug = this.slug.value,
            description = this.description
        )
    }

    fun ProjectEntity.toDomain(): Project {
        return Project(
            id = this.id,
            name = this.name,
            slug = Slug.of(this.slug),
            description = this.description
        )
    }
}
