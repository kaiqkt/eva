package dev.kaiqkt.eva.adapter.outbound.persistence.entity

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "applications")
class ApplicationEntity(
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    @OneToOne(cascade = [CascadeType.ALL], optional = false)
    @JoinColumn(name = "repository_id", nullable = false, unique = true)
    val repository: RepositoryEntity = RepositoryEntity(),
    @Id
    val id: String = UlidCreator.getMonotonicUlid().toString()
)
