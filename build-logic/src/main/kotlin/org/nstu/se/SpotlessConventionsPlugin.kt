package org.nstu.se

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class SpotlessConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("com.diffplug.spotless")

        val libs = target.extensions
            .getByType<VersionCatalogsExtension>()
            .named("libs")

        val gjfVersion = libs.findVersion("googleJavaFormat").get().requiredVersion

        target.extensions.configure<SpotlessExtension> {
            java {
                target("src/**/*.java")
                googleJavaFormat(gjfVersion)
                    .aosp()
                    .reorderImports(true)
                removeUnusedImports()
                trimTrailingWhitespace()
                endWithNewline()
                toggleOffOn()
            }
            format("misc") {
                target(
                    "**/*.md",
                    "**/*.gradle.kts",
                    "**/*.yml",
                    "**/*.yaml",
                    "**/*.toml",
                    "**/*.properties",
                    "**/.editorconfig",
                    "**/.gitignore",
                    "**/.gitattributes",
                    "**/.githooks/*",
                )
                targetExclude(
                    "**/build/**",
                    "**/.gradle/**",
                    "**/gradlew",
                    "**/gradlew.bat",
                )
                trimTrailingWhitespace()
                endWithNewline()
            }
        }
    }
}
