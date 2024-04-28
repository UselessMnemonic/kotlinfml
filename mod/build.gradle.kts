val javaVersion get() = JavaLanguageVersion.of(property("javaVersion") as String)
val minecraftVersion get() = property("minecraftVersion") as String
val neoVersion get() = property("neoVersion") as String
val forgeDependencyVersion get() = "$minecraftVersion-$neoVersion"

version = rootProject.version
group = rootProject.group

plugins {
    kotlin("jvm") version "1.9.0"
}

kotlin {
    jvmToolchain(javaVersion.asInt())
}

dependencies {
    compileOnly("net.neoforged:fmlcore:$forgeDependencyVersion")
    compileOnly("net.neoforged:fmlloader:$forgeDependencyVersion")
    compileOnly("net.neoforged:javafmllanguage:$forgeDependencyVersion")
    compileOnly("com.mojang:logging")
    compileOnly(project(":loader"))
}

tasks.processResources {
    filesMatching("META-INF/mods.toml") {
        expand(properties)
    }
}

tasks.jar {
    archiveBaseName = "${rootProject.name}-${project.name}"
    manifest {
        attributes(
            "Specification-Version" to "1",
            "Specification-Vendor" to "Forge",
            "Specification-Title" to project.property("modDisplayName"),
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to project.property("authors")
        )
    }
}
