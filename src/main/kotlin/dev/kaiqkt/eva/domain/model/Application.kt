package dev.kaiqkt.eva.domain.model

data class Application(
    val name: String,
    val slug: String,
    val description: String?,
    val projectId: String,
    val repository: GitRepository
)
