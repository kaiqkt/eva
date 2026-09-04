package dev.kaiqkt.eva.adapter.outbound.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "projects")
class ProjectEntity(
    @Id
    private val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String? = null
) : PersistableEntity() {
    override fun getId(): String = id
}
