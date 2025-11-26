import org.gradle.kotlin.dsl.dependencies

plugins {
    kotlin("jvm") version "2.2.20"
    id("com.gradleup.shadow") version "8.3.9"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

group = "me.cirnoslab"
version = "1.1.0-SNAPSHOT"

repositories {
    flatDir {
        dirs("libs")
    }
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")
    compileOnly(":spigot-1.8.8") // 1.8 full API
    implementation(":TextAPI") // bintray shut down 草
    implementation("dev.dejvokep:boosted-yaml:1.3.7")
    implementation("fr.mrmicky:fastboard:2.1.5")
    testImplementation(kotlin("test"))
}

tasks.shadowJar {
    relocate("io.github.theluca98.textapi", "me.cirnoslab.smashsumo.libs.textapi")
    relocate("dev.dejvokep.boostedyaml", "me.cirnoslab.smashsumo.libs.boostedyaml")
    relocate("fr.mrmicky.fastboard", "me.cirnoslab.smashsumo.libs.fastboard")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
