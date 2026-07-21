package dev.kaiqkt.eva.application.usecase

import dev.kaiqkt.eva.application.port.outbound.ApplicationRepositoryPort
import dev.kaiqkt.eva.domain.exception.ApplicationAlreadyExistsException
import dev.kaiqkt.eva.domain.model.Application
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CreateApplicationServiceTest {

    private class FakeApplicationRepository : ApplicationRepositoryPort {
        val saved = mutableListOf<Application>()
        var existing = false

        override fun save(application: Application): Application {
            saved.add(application)
            return application.copy(id = "01ARZ3NDEKTSV4RRFFQ69G5FAV")
        }

        override fun existsBySlug(slug: String): Boolean = existing
    }

    private val repository = FakeApplicationRepository()
    private val service = CreateApplicationService(repository)

    @Test
    fun shouldCreateApplicationGeneratingSlugFromName() {
        val result = service.create("Minha App", "descricao")

        assertEquals("minha-app", result.slug)
        assertEquals("Minha App", result.name)
        assertEquals("descricao", result.description)
        assertEquals(1, repository.saved.size)
    }

    @Test
    fun shouldRejectWhenSlugAlreadyExists() {
        repository.existing = true

        assertFailsWith<ApplicationAlreadyExistsException> {
            service.create("Minha App", null)
        }
        assertTrue(repository.saved.isEmpty())
    }
}
