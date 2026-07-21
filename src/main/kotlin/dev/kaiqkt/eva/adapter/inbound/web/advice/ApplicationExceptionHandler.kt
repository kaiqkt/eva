package dev.kaiqkt.eva.adapter.inbound.web.advice

import dev.kaiqkt.eva.domain.exception.ApplicationAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApplicationExceptionHandler {

    @ExceptionHandler(ApplicationAlreadyExistsException::class)
    fun handleAlreadyExists(exception: ApplicationAlreadyExistsException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            exception.message ?: "Application already exists",
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
}
