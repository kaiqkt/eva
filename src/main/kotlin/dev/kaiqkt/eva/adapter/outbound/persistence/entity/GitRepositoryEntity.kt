package dev.kaiqkt.eva.adapter.outbound.persistence.entity

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "repositories")
class GitRepositoryEntity(
    val url: String = "",
    @Id
    private val id: String = UlidCreator.getMonotonicUlid().toString()
) : PersistableEntity() {
    override fun getId(): String = id
}
