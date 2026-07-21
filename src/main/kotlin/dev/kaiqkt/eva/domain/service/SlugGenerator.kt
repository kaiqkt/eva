package dev.kaiqkt.eva.domain.service

object SlugGenerator {
    fun fromName(name: String): String =
        name.trim().lowercase().replace(" ", "-")
}
