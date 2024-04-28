val javaVersion get() = JavaLanguageVersion.of(property("javaVersion") as String)
val minecraftVersion get() = property("minecraftVersion") as String
val neoVersion get() = property("neoVersion") as String
val forgeDependencyVersion get() = "$minecraftVersion-$neoVersion"
val publicationName = "${rootProject.name}-${project.name}"

version = "1.0"
group = rootProject.group

plugins {
    signing
    `maven-publish`
    kotlin("jvm") version "1.9.0"
}

kotlin {
    jvmToolchain(javaVersion.asInt())
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

tasks.jar {
    archiveBaseName = publicationName
}

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            from(components["java"])
            artifactId = publicationName
            pom {
                name = publicationName
                description = project.description
                url = "https://github.com/UselessMnemonic/kotlinfml"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://github.com/UselessMnemonic/kotlinfml/blob/main/LICENSE.md"
                    }
                }
                developers {
                    developer {
                        id = "UselessMnemonic"
                        name = "Christopher Madrigal"
                        email = "chrisjmadrigal AT gmail DOT com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/UselessMnemonic/kotlinfml"
                    url = "https://github.com/UselessMnemonic/kotlinfml"
                }
            }
        }
    }
}

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val singingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingKeyId, singingPassword)
    //sign(publishing.publications[publicationName])
}