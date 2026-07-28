package dev.kaiqkt.eva.domain.model

data class Project(
    val name: String,
    val slug: String,
    val description: String?,
    val id: String? = null
)
