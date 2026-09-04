package dev.kaiqkt.eva.adapter.outbound.forgejo.client.response

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateRepositoryResponse(
    @param:JsonProperty("html_url")
    val htmlUrl: String
)
