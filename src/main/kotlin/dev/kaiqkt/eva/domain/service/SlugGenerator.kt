package dev.kaiqkt.eva.domain.service

import com.github.slugify.Slugify

object SlugGenerator {
    private val slugify = Slugify.builder().build()

    fun fromName(name: String): String = slugify.slugify(name)
}
