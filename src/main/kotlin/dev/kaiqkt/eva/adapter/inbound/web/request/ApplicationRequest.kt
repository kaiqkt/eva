package dev.kaiqkt.eva.adapter.inbound.web.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ApplicationRequest(
    @field:NotBlank
    val projectSlug: String,
    @field:NotBlank
    @field:Size(max = RequestConstraints.NAME_MAX)
    @field:Pattern(regexp = "^[a-zA-Z0-9 ]+$")
    val name: String,
    @field:Size(max = RequestConstraints.DESCRIPTION_MAX)
    val description: String?
)
