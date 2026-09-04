package dev.kaiqkt.eva.adapter.inbound.web.advice

import dev.kaiqkt.eva.application.port.outbound.CodeHostingException
import dev.kaiqkt.eva.domain.exception.DomainException
import dev.kaiqkt.eva.domain.exception.ErrorType
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DomainException::class)
    fun handleDomain(exception: DomainException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            exception.type.toHttpStatus(),
            exception.message ?: "Unexpected error",
        )

    @ExceptionHandler(CodeHostingException::class)
    fun handleCodeHosting(): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY,
            "Code hosting provider is unavailable",
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException): ProblemDetail {
        val errors = exception.bindingResult.fieldErrors.map { error ->
            mapOf(
                "field" to error.field,
                "message" to (error.defaultMessage ?: "invalid value"),
            )
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed").apply {
            setProperty("errors", errors)
        }
    }

    private fun ErrorType.toHttpStatus(): HttpStatus = when (this) {
        ErrorType.ALREADY_EXISTS -> HttpStatus.CONFLICT
        ErrorType.NOT_FOUND -> HttpStatus.NOT_FOUND
    }
}
