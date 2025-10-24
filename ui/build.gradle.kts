plugins {
    kotlin("jvm") version "1.9.23"
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "1.9.23"
}

application {
    mainClass.set("org.example.MainKt")
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":common"))

    implementation("io.ktor:ktor-server-core-jvm:2.3.5")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.5")
    implementation("io.ktor:ktor-server-auth:2.3.5")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    implementation("io.ktor:ktor-server-cors:2.3.5")
    implementation("io.ktor:ktor-server-swagger:2.3.5")

    implementation("io.insert-koin:koin-ktor:3.5.0")
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.0")

    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")

    implementation("net.datafaker:datafaker:2.0.2")
}

tasks {
    shadowJar {
        archiveBaseName.set("smart-parking")
        archiveClassifier.set("")
        archiveVersion.set("")
        mergeServiceFiles()
    }

    val copyToRootBuild by registering(Copy::class) {
        dependsOn(shadowJar)
        from(shadowJar.get().archiveFile)
        into("$rootDir/build")
        rename { "smart-parking.jar" }
    }

    build {
        dependsOn(copyToRootBuild)
    }
}

kotlin {
    jvmToolchain(21)
}
