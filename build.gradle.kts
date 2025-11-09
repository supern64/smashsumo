import org.gradle.kotlin.dsl.dependencies

plugins {
    kotlin("jvm") version "2.2.20"
    id("com.gradleup.shadow") version "9.2.2"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

group = "me.cirnoslab"
version = "1.0M-SNAPSHOT"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    implementation("dev.dejvokep:boosted-yaml:1.3.7")
    implementation("fr.mrmicky:fastboard:2.1.5")
    testImplementation(kotlin("test"))
}

tasks.shadowJar {
    relocate("dev.dejvokep.boostedyaml", "me.cirnoslab.smashsumo.libs.boostedyaml")
    relocate("fr.mrmicky.fastboard", "me.cirnoslab.smashsumo.libs.fastboard")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
