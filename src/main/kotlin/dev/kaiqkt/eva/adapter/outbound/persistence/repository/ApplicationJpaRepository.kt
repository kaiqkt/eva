package dev.kaiqkt.eva.adapter.outbound.persistence.repository

import dev.kaiqkt.eva.adapter.outbound.persistence.entity.ApplicationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationJpaRepository : JpaRepository<ApplicationEntity, String> {
    fun existsBySlug(slug: String): Boolean
}
