import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    application
}

group = "eu.wojciechzurek.eu"
version = "1.0-SNAPSHOT"

val client: Boolean by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "16"
}

application {
    if (project.hasProperty("client")) {
        val run: JavaExec by tasks
        run.standardInput = System.`in`
        mainClassName = "eu.wojciechzurek.example.ClientKt"
    } else {
        mainClassName = "eu.wojciechzurek.example.ServerKt"
    }
}
