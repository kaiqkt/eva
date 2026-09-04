package dev.kaiqkt.eva.adapter.outbound.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "applications")
class ApplicationEntity(
    @Id
    private val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    @Column(name = "project_id", nullable = false)
    val projectId: String = "",
    @OneToOne(cascade = [CascadeType.ALL], optional = false)
    @JoinColumn(name = "repository_id", nullable = false, unique = true)
    val repository: GitRepositoryEntity = GitRepositoryEntity()
) : PersistableEntity() {
    override fun getId(): String = id
}
