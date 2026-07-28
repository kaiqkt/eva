package dev.kaiqkt.eva.adapter.inbound.web.response

import dev.kaiqkt.eva.domain.model.Project

data class ProjectResponse(
    val name: String,
    val slug: String,
    val description: String?
) {
    companion object {
        fun from(project: Project) = ProjectResponse(
            name = project.name,
            slug = project.slug,
            description = project.description,
        )
    }
}
