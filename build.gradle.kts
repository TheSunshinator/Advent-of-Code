plugins {
    kotlin("jvm") version "1.7.22"
    id("com.google.devtools.ksp") version "1.7.22-1.0.8"
}

repositories {
    mavenCentral()
}

tasks {
    sourceSets.main {
        java.srcDirs("src")
    }

    wrapper {
        gradleVersion = "7.5.1"
    }
}

kotlin.sourceSets.main {
    kotlin.srcDirs("build/generated/ksp/main/kotlin")
}

dependencies {
    implementation("io.arrow-kt:arrow-core:1.1.3")
    implementation("io.arrow-kt:arrow-optics:1.1.3")
    ksp("io.arrow-kt:arrow-optics-ksp-plugin:1.1.3")

    implementation("io.kotest:kotest-assertions-core:5.5.4")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}
