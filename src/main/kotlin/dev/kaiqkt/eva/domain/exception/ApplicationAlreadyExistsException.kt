package dev.kaiqkt.eva.domain.exception

class ApplicationAlreadyExistsException(slug: String) :
    RuntimeException("Application with slug '$slug' already exists")
