plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

val javaVersion = libs.versions.java.get().toInt()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

kotlin {
    jvmToolchain(javaVersion)
}

dependencies {
    implementation(libs.javafx.plugin)
    implementation(libs.spotless.plugin)
    implementation(libs.spotbugs.plugin)
    implementation(libs.errorprone.plugin)
}

gradlePlugin {
    plugins {
        create("javaConventions") {
            id = "se-labs.java-conventions"
            implementationClass = "org.nstu.se.JavaConventionsPlugin"
        }
        create("javafxConventions") {
            id = "se-labs.javafx-conventions"
            implementationClass = "org.nstu.se.JavaFxConventionsPlugin"
        }
        create("testConventions") {
            id = "se-labs.test-conventions"
            implementationClass = "org.nstu.se.TestConventionsPlugin"
        }
        create("qualityConventions") {
            id = "se-labs.quality-conventions"
            implementationClass = "org.nstu.se.QualityConventionsPlugin"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
}
