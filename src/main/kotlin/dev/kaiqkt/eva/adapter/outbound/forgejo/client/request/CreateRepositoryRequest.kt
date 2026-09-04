package dev.kaiqkt.eva.adapter.outbound.forgejo.client.request

data class CreateRepositoryRequest(
    val name: String,
    val description: String? = null,
    val private: Boolean = true,
    val autoInit: Boolean = true,
    val defaultBranch: String = "main",
)
