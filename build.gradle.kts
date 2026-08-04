plugins {
    kotlin("jvm") version "2.2.21"
}

group = "de.winkler"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.ortools:ortools-java:9.15.6755")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}