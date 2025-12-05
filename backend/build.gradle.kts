plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.23" apply false
    id("io.qameta.allure") version "2.11.2"
}

group = "org.example"
version = "1.0"

repositories {
    google()
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("net.bytebuddy:byte-buddy:1.14.12")
}