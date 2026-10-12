package org.nstu.se

import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class QualityConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(JavaConventionsPlugin::class.java)
        target.pluginManager.apply("checkstyle")
        target.pluginManager.apply("pmd")
        target.pluginManager.apply("com.diffplug.spotless")
        target.pluginManager.apply("com.github.spotbugs")

        val libs = target.extensions
            .getByType<VersionCatalogsExtension>()
            .named("libs")

        val gjfVersion = libs.findVersion("googleJavaFormat").get().requiredVersion
        val checkstyleVersion = libs.findVersion("checkstyle").get().requiredVersion
        val pmdVersion = libs.findVersion("pmd").get().requiredVersion
        val spotbugsVersion = libs.findVersion("spotbugs").get().requiredVersion

        target.extensions.configure<SpotlessExtension> {
            java {
                target("src/**/*.java")
                googleJavaFormat(gjfVersion)
                    .aosp()
                    .reorderImports(true)
                removeUnusedImports()
                trimTrailingWhitespace()
                endWithNewline()
            }
            format("misc") {
                target("*.md", "*.gradle.kts", ".editorconfig", ".gitignore")
                trimTrailingWhitespace()
                endWithNewline()
            }
        }

        val checkstyleConfig = target.rootProject.file("config/checkstyle/checkstyle.xml")
        target.extensions.configure<CheckstyleExtension> {
            toolVersion = checkstyleVersion
            isIgnoreFailures = false
            maxWarnings = 0
            if (checkstyleConfig.exists()) {
                config = target.resources.text.fromFile(checkstyleConfig)
            }
        }

        val pmdConfig = target.rootProject.file("config/pmd/ruleset.xml")
        target.extensions.configure<PmdExtension> {
            toolVersion = pmdVersion
            isConsoleOutput = true
            if (pmdConfig.exists()) {
                ruleSetConfig = target.resources.text.fromFile(pmdConfig)
            }
        }

        target.extensions.configure<SpotBugsExtension> {
            toolVersion.set(spotbugsVersion)
            effort.set(Effort.MAX)
            reportLevel.set(Confidence.MEDIUM)
        }

        target.tasks.matching { it.name == "spotbugsTest" }.configureEach {
            enabled = false
        }
    }
}
