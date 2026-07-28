package dev.kaiqkt.eva.application.port.inbound

import dev.kaiqkt.eva.domain.model.Project

interface CreateProjectUseCase {
    fun create(name: String, description: String?): Project

}
