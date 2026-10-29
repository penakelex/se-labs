plugins {
    id("se-labs.javafx-conventions")
    id("se-labs.test-conventions")
    id("se-labs.quality-conventions")
}

application {
    mainClass = "org.nstu.se.lab2.Launcher"
}

dependencies {
    pmdAux(
        javaToolchains.launcherFor(java.toolchain)
            .map { it.metadata.installationPath }
            .map { it.files("lib/jrt-fs.jar") }
    )
}
