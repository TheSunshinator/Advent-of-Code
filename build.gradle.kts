plugins {
    kotlin("jvm") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}

tasks {
    sourceSets.main {
        java.srcDirs("src")
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

    implementation("com.google.code.gson:gson:2.10")
}
