package dev.kaiqkt.eva.adapter.outbound.persistence.jpa

import dev.kaiqkt.eva.adapter.outbound.persistence.entity.ProjectEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectJpaRepository : JpaRepository<ProjectEntity, String> {
    fun existsBySlug(slug: String): Boolean
    fun findBySlug(slug: String): ProjectEntity?
}
