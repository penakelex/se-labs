import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
        create("spotlessConventions") {
            id = "se-labs.spotless-conventions"
            implementationClass = "org.nstu.se.SpotlessConventionsPlugin"
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(
        JvmTarget.fromTarget(javaVersion.toString())
    )
}
