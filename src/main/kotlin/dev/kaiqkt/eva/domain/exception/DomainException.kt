package dev.kaiqkt.eva.domain.exception

abstract class DomainException(
    val type: ErrorType,
    message: String
) : RuntimeException(message)
