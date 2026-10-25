package org.nstu.se

import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension

class JavaConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(JavaPlugin::class.java)
        target.pluginManager.apply("jacoco")
        target.pluginManager.apply("net.ltgt.errorprone")

        val libs = target.extensions
            .getByType<VersionCatalogsExtension>()
            .named("libs")

        val javaVersion = libs.findVersion("java").get().requiredVersion.toInt()
        val jacocoVersion = libs.findVersion("jacoco").get().requiredVersion

        target.extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.toVersion(javaVersion)
            targetCompatibility = JavaVersion.toVersion(javaVersion)
            toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }

        target.dependencies.add("errorprone", libs.findLibrary("errorprone-core").get())
        target.dependencies.add("errorprone", libs.findLibrary("nullaway").get())
        target.dependencies.add("compileOnly", libs.findLibrary("jspecify").get())
        target.dependencies.add("testCompileOnly", libs.findLibrary("jspecify").get())

        val nullawayEnabled = target.providers.gradleProperty("nullaway").orNull != "false"

        target.tasks.withType<JavaCompile>().configureEach {
            val isTestCompile = name.startsWith("compileTest")

            options.encoding = "UTF-8"
            options.release.set(javaVersion)
            options.compilerArgs.add("-parameters")

            options.errorprone {
                disableWarningsInGeneratedCode.set(true)
                check("MissingOverride", CheckSeverity.ERROR)
                check("EqualsHashCode", CheckSeverity.ERROR)
                check("UnusedVariable", CheckSeverity.WARN)

                when {
                    isTestCompile -> disable("NullAway")
                    nullawayEnabled -> {
                        check("NullAway", CheckSeverity.ERROR)
                        option("NullAway:AnnotatedPackages", "org.nstu.se")
                        option("NullAway:JSpecifyMode", "true")
                        option("NullAway:AcknowledgeRestrictiveAnnotations", "true")
                    }
                }
            }
        }

        target.extensions.configure<JacocoPluginExtension> {
            toolVersion = jacocoVersion
        }

        target.tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}
