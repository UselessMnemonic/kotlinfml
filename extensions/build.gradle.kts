plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish") version "0.28.0"
}

kotlin {
    jvmToolchain(project.javaVersion.asInt())
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnly("net.neoforged:fmlcore:$forgeDependencyVersion")
    compileOnly("net.neoforged:fmlloader:$forgeDependencyVersion")
    compileOnly("com.mojang:logging")
    compileOnly(project(":loader"))
}

tasks.withType(Jar::class.java) {
    archiveBaseName = publicationName
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    pom {
        name = publicationName
        description = "Extensions for kotlinfml mods."
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
