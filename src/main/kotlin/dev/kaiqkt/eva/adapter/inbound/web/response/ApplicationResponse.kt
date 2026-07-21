package dev.kaiqkt.eva.adapter.inbound.web.response

import dev.kaiqkt.eva.domain.model.Application

data class ApplicationResponse(
    val id: String,
    val name: String,
    val slug: String,
    val description: String?
) {
    companion object {
        fun from(application: Application) = ApplicationResponse(
            id = requireNotNull(application.id) { "persisted Application must have an id" },
            name = application.name,
            slug = application.slug,
            description = application.description,
        )
    }
}
