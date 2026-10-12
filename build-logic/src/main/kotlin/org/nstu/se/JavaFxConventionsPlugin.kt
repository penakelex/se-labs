package org.nstu.se

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.openjfx.gradle.JavaFXOptions

class JavaFxConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(JavaConventionsPlugin::class.java)
        target.pluginManager.apply("application")
        target.pluginManager.apply("org.openjfx.javafxplugin")

        val libs = target.extensions
            .getByType<VersionCatalogsExtension>()
            .named("libs")

        val javafxVersion = libs.findVersion("javafx").get().requiredVersion

        target.extensions.configure<JavaFXOptions> {
            version = javafxVersion
            modules = listOf("javafx.controls", "javafx.fxml")
        }
    }
}
