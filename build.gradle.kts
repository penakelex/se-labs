allprojects {
    group = "org.nstu.se"
    version = "1.0.0"
}

tasks.register<Exec>("installHooks") {
    group = "setup"
    description = "Configure git to use hooks from .githooks/"
    commandLine("git", "config", "core.hooksPath", ".githooks")
}
