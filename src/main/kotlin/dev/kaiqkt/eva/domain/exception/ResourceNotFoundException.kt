package dev.kaiqkt.eva.domain.exception

class ResourceNotFoundException(resource: String) :
    DomainException(ErrorType.NOT_FOUND, "$resource not found")
