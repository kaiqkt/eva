package dev.kaiqkt.eva.application.port.inbound

import dev.kaiqkt.eva.domain.model.Application

interface CreateApplicationUseCase {
    fun create(projectSlug: String, name: String, description: String?): Application
}
