package dev.kaiqkt.eva.adapter.inbound.web.request

import dev.kaiqkt.eva.adapter.inbound.web.support.RequestConstants
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

private const val NAME_MAX = 50

data class ProjectRequest(
    @field:NotBlank
    @field:Size(max = NAME_MAX)
    @field:Pattern(regexp = "^[a-zA-Z0-9 ]+$")
    val name: String,
    @field:Size(max = RequestConstants.CHAR_MAX)
    val description: String?
)
