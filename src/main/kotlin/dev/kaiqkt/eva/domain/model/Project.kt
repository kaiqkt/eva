package dev.kaiqkt.eva.domain.model

import com.github.f4b6a3.ulid.UlidCreator

data class Project(
    val id: String,
    val name: String,
    val slug: Slug,
    val description: String?
) {
    companion object {
        fun create(name: String, description: String?): Project = Project(
            id = UlidCreator.getMonotonicUlid().toString(),
            name = name,
            slug = Slug.fromName(name),
            description = description
        )
    }
}
