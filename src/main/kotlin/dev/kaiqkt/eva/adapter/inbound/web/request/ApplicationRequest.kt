package dev.kaiqkt.eva.adapter.inbound.web.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ApplicationRequest(
    @field:NotBlank
    @field:Size(max = RequestConstraints.SLUG_MAX)
    @field:Pattern(regexp = RequestConstraints.SLUG_FORMAT)
    val projectSlug: String,
    @field:NotBlank
    @field:Size(max = RequestConstraints.NAME_MAX)
    @field:Pattern(regexp = RequestConstraints.NAME_FORMAT)
    val name: String,
    @field:Size(max = RequestConstraints.DESCRIPTION_MAX)
    val description: String?
)
