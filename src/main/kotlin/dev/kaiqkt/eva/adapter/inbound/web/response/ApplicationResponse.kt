package dev.kaiqkt.eva.adapter.inbound.web.response

import dev.kaiqkt.eva.domain.model.Application

data class ApplicationResponse(
    val name: String,
    val slug: String,
    val description: String?
) {
    companion object {
        fun from(application: Application) = ApplicationResponse(
            name = application.name,
            slug = application.slug.value,
            description = application.description,
        )
    }
}
