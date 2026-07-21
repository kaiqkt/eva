package dev.kaiqkt.eva

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class EvaApplication

// spread do args é o idioma canônico do Spring Boot main — sem alternativa
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<EvaApplication>(*args)
}
