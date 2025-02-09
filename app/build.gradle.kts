@file:Suppress("UnstableApiUsage", "unused")

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.lucene.core)
    implementation(libs.lucene.queryparser)
    implementation(libs.clikt)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit.jupiter.get())
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "luceneinaction.AppKt"
    applicationName = "luca"
    applicationDefaultJvmArgs = listOf(
        "--add-modules=jdk.incubator.vector",
        "--enable-preview"
    )
}

