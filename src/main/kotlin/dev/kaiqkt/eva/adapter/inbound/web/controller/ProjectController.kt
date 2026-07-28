package dev.kaiqkt.eva.adapter.inbound.web.controller

import dev.kaiqkt.eva.adapter.inbound.web.request.ProjectRequest
import dev.kaiqkt.eva.adapter.inbound.web.response.ProjectResponse
import dev.kaiqkt.eva.application.port.inbound.CreateProjectUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val createProjectUseCase: CreateProjectUseCase,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: ProjectRequest,
        uriBuilder: UriComponentsBuilder,
    ): ResponseEntity<ProjectResponse> {
        val project = createProjectUseCase.create(request.name, request.description)
        val response = ProjectResponse.from(project)
        val location = uriBuilder.path("/projects/{slug}").buildAndExpand(response.slug).toUri()
        return ResponseEntity.created(location).body(response)
    }
}
