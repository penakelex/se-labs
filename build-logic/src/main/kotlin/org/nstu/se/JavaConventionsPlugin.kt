package org.nstu.se

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

        target.tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(javaVersion)
            options.errorprone {
                disableWarningsInGeneratedCode.set(true)
                check("MissingOverride", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
                check("EqualsHashCode", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
                check("UnusedVariable", net.ltgt.gradle.errorprone.CheckSeverity.WARN)
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
