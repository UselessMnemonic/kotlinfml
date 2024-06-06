plugins {
    kotlin("jvm")
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
    archiveBaseName = publicationName
    manifest {
        attributes(
            "Specification-Version" to "1",
            "Specification-Vendor" to "Forge",
            "Specification-Title" to modDisplayName,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to modAuthors,
        )
    }
}
