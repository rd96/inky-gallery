plugins {
    kotlin("jvm") version "2.4.0"
}

group = "uk.derbyshire"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.http4k.bom))
    implementation(libs.bundles.http4k)

    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.result4k)

    implementation(libs.bundles.exposed)
    implementation(libs.postgres)
    implementation(libs.hikari)

    implementation(libs.argon2)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}