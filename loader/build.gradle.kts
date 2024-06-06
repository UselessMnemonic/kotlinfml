plugins {
    java
    kotlin("jvm") apply false
    id("com.vanniktech.maven.publish") version "0.28.0"
}

java {
    toolchain {
        languageVersion.set(javaVersion)
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnly("net.neoforged:fmlcore:$forgeDependencyVersion")
    compileOnly("net.neoforged:fmlloader:$forgeDependencyVersion")
    compileOnly("net.neoforged:javafmllanguage:$forgeDependencyVersion")
    compileOnly("com.mojang:logging")
}

tasks.withType(Jar::class.java) {
    archiveBaseName = publicationName
}

tasks.jar {
    manifest {
        attributes(
            "Specification-Version" to "1",
            "Specification-Vendor" to "Forge",
            "Specification-Title" to modDisplayName,
            "Implementation-Version" to version,
            "Implementation-Vendor" to modAuthors,
            "FMLModType" to "LANGPROVIDER"
        )
    }
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    pom {
        name = publicationName
        description = modDescription
        url = gitUrl
        licenses {
            license {
                name = modLicense
            }
        }
        developers {
            developer {
                id = "UselessMnemonic"
                name = "Christopher Madrigal"
                email = "chrisjmadrigal@gmail.com"
            }
        }
        scm {
            connection = gitConnection
            url = gitUrl
        }
    }
}
