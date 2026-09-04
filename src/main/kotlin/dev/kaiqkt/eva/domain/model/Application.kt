package dev.kaiqkt.eva.domain.model

import com.github.f4b6a3.ulid.UlidCreator

data class Application(
    val id: String,
    val name: String,
    val slug: Slug,
    val description: String?,
    val projectId: String,
    val repository: GitRepository
) {
    companion object {
        fun create(
            name: String,
            description: String?,
            projectId: String,
            repository: GitRepository
        ): Application = Application(
            id = UlidCreator.getMonotonicUlid().toString(),
            name = name,
            slug = Slug.fromName(name),
            description = description,
            projectId = projectId,
            repository = repository
        )
    }
}
