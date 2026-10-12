package org.nstu.se

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.getByType

class TestConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(JavaPlugin::class.java)

        val libs = target.extensions
            .getByType<VersionCatalogsExtension>()
            .named("libs")

        target.dependencies.add("testImplementation", libs.findLibrary("junit-jupiter").get())
        target.dependencies.add("testRuntimeOnly", libs.findLibrary("junit-platform-launcher").get())
        target.dependencies.add("testImplementation", libs.findLibrary("assertj-core").get())
        target.dependencies.add("testImplementation", libs.findLibrary("mockito-core").get())
        target.dependencies.add("testImplementation", libs.findLibrary("mockito-junit-jupiter").get())
    }
}
