package dev.kaiqkt.eva.domain.exception

class ResourceAlreadyExistsException(resource: String) :
    DomainException(ErrorType.ALREADY_EXISTS, "$resource already exists")
