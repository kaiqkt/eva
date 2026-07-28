package dev.kaiqkt.eva.adapter.outbound.persistence.mapper

import dev.kaiqkt.eva.adapter.outbound.persistence.entity.ProjectEntity
import dev.kaiqkt.eva.domain.model.Project

object ProjectMapper {
    fun Project.toEntity(): ProjectEntity {
        return ProjectEntity(
            name = this.name,
            slug = this.slug,
            description = this.description
        )
    }

    fun ProjectEntity.toDomain(): Project {
        return Project(
            name = this.name,
            slug = this.slug,
            description = this.description,
            id = this.id
        )
    }
}
