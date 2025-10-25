plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "MyParking"
include("common")
include("core")
include("data")
include("ui")
include("integration_tests")
include("e2e_tests")