plugins {
    kotlin("jvm") version "1.7.21"
}

repositories {
    mavenCentral()
}

tasks {
    sourceSets {
        main {
            java.srcDirs("src")
        }
    }

    wrapper {
        gradleVersion = "7.5.1"
    }
}

dependencies {
    implementation("io.arrow-kt:arrow-core:1.1.2")
    implementation("io.kotest:kotest-assertions-core:5.5.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}
