package dev.kaiqkt.eva.adapter.outbound.persistence.entity

import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable

@MappedSuperclass
abstract class PersistableEntity : Persistable<String> {

    @Transient
    private var persisted: Boolean = false

    override fun isNew(): Boolean = !persisted

    @PostLoad
    @PostPersist
    fun markPersisted() {
        persisted = true
    }
}
