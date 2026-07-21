import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.detekt)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.flyway.core)
    implementation(libs.ulid.creator)
    implementation(libs.caffeine)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.database.postgresql)
    runtimeOnly(libs.h2)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(kotlin("test"))
    detektPlugins(libs.detekt.formatting)
}

// commons-lang3 vem transitivo do springdoc (swagger-core-jakarta) em 3.17.0 —
// CVE-2025-48924 (recursão descontrolada em ClassUtils.getClass, StackOverflow DoS),
// fixado em 3.18.0. Springdoc ainda não bumpou; pinamos até bumpar.
dependencyManagement {
    dependencies {
        dependency("org.apache.commons:commons-lang3:${libs.versions.commonsLang3.get()}")
    }
}

tasks.withType<Test> { useJUnitPlatform() }

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    parallel = true
}

// detekt 1.23.x roda com Kotlin 2.0.21; o KGP 2.2.0 vaza pro classpath dele e
// quebra. Fixa a versão do compiler só na configuração do detekt (doc oficial).
configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("2.0.21")
        }
    }
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}
