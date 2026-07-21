package dev.kaiqkt.eva.adapter.outbound.persistence.entity

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "applications")
class ApplicationEntity(
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    @Id
    val id: String = UlidCreator.getMonotonicUlid().toString()
)
