package dev.kaiqkt.eva.domain.model

import com.github.slugify.Slugify

@JvmInline
value class Slug private constructor(val value: String) {

    override fun toString(): String = value

    companion object {
        private const val MAX_LENGTH = 50
        private val SLUGIFY = Slugify.builder().build()
        private val FORMAT = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")

        fun fromName(name: String): Slug = of(SLUGIFY.slugify(name))

        fun of(value: String): Slug {
            require(value.length <= MAX_LENGTH) { "slug exceeds $MAX_LENGTH characters" }
            require(FORMAT.matches(value)) { "slug does not match $FORMAT" }
            return Slug(value)
        }
    }
}
