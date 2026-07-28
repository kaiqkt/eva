package dev.kaiqkt.eva.adapter.inbound.web.controller

import dev.kaiqkt.eva.adapter.inbound.web.request.ApplicationRequest
import dev.kaiqkt.eva.adapter.inbound.web.response.ApplicationResponse
import dev.kaiqkt.eva.application.port.inbound.CreateApplicationUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/applications")
class ApplicationController(
    private val createApplicationUseCase: CreateApplicationUseCase,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: ApplicationRequest,
        uriBuilder: UriComponentsBuilder,
    ): ResponseEntity<ApplicationResponse> {
        val application = createApplicationUseCase.create(request.projectSlug, request.name, request.description)
        val response = ApplicationResponse.from(application)
        val location = uriBuilder.path("/applications/{slug}").buildAndExpand(response.slug).toUri()
        return ResponseEntity.created(location).body(response)
    }
}
