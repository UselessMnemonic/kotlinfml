plugins {
    kotlin("jvm") version "1.9.20"
}

allprojects {

    val modAuthors = mutableSetOf<String>()
    modAuthors.add("UselessMnemonic")

    extensions.add("modDisplayName", "Kotlin Mod Loader")
    extensions.add("modAuthors", modAuthors.joinToString(", "))
    extensions.add("modDescription", "A BYO Kotlin loader for Kotlin-based mods.")
    extensions.add("modLicense", "MIT")

    val minecraftVersion = "1.20.1"
    extensions.add("minecraftVersion", minecraftVersion)
    extensions.add("minecraftVersionRange", "[$minecraftVersion,1.21)")

    val neoVersion = "47.1.1"
    extensions.add("neoVersion", neoVersion)
    extensions.add("neoVersionRange", "[47.1,)")

    val modVersion = "0.1.0"
    extensions.add("modVersion", modVersion)

    val isRelease = System.getenv("RELEASE") == "true"
    version = if (isRelease) modVersion else "$modVersion-SNAPSHOT"
    group = "com.uselessmnemonic"

    extensions.add("javaVersion", JavaLanguageVersion.of(17))
    extensions.add("forgeDependencyVersion", "$minecraftVersion-$neoVersion")
    extensions.add("publicationName", "${rootProject.name}-${project.name}")

    repositories {
        maven("https://maven.neoforged.net/releases")
        maven("https://libraries.minecraft.net")
    }

    val ossrhUri = if (isRelease)
        uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
    else
        uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    extensions.add("ossrhUri", ossrhUri)

    extensions.add("gitUrl", "https://github.com/UselessMnemonic/kotlinfml")
    extensions.add("gitConnection", "scm:git:git://github.com/UselessMnemonic/kotlinfml")
}
