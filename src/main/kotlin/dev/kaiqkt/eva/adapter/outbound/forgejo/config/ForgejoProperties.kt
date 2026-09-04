package dev.kaiqkt.eva.adapter.outbound.forgejo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "forgejo")
data class ForgejoProperties(
    val baseUrl: String,
    val token: String,
    val connectTimeout: Duration,
    val readTimeout: Duration
)
