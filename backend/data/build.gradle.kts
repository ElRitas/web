plugins {
    kotlin("jvm") version "1.9.23"
    id("io.qameta.allure") version "2.11.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")

    testImplementation(kotlin("test"))
    testImplementation("io.qameta.allure:allure-junit5:2.22.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

allure {
    adapter {
        frameworks {
            junit5 {
                adapterVersion.set("2.22.0")
            }
        }
    }
}

kotlin {
    jvmToolchain(21)
}