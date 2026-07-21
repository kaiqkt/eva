package dev.kaiqkt.eva.adapter.inbound.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.kaiqkt.eva.adapter.inbound.web.request.ApplicationRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ApplicationControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun shouldCreateApplicationAndReturn201() {
        val request = ApplicationRequest(name = "Minha App", description = "desc real")

        mockMvc.post("/applications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            jsonPath("$.id") { exists() }
            jsonPath("$.slug") { value("minha-app") }
            jsonPath("$.description") { value("desc real") }
        }
    }

    @Test
    fun shouldReturn409OnDuplicateSlug() {
        val body = objectMapper.writeValueAsString(
            ApplicationRequest(name = "Dup App", description = null),
        )

        mockMvc.post("/applications") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isCreated() } }

        mockMvc.post("/applications") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isConflict() } }
    }

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun datasourceProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
