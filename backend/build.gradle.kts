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
    implementation(libs.bundles.flyway)

    implementation(libs.argon2)

    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.params)
    testImplementation(libs.mockk)
}

tasks.register<JavaExec>("runMigrationGenerator") {
    group = "database"
    description = "Generates suggested SQL for the next Flyway migration"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("uk.derbyshire.inkygallery.tooling.GenerateMigration")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}