package dev.kaiqkt.eva.adapter.outbound.forgejo.client.request

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateRepositoryRequest(
    val name: String,
    val description: String? = null,
    val private: Boolean = true,
    @get:JsonProperty("auto_init")
    val autoInit: Boolean = true,
    @get:JsonProperty("default_branch")
    val defaultBranch: String = "main",
)
