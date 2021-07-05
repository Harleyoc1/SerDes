fun property(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("maven-publish")
}

val projectName = property("name")
group = property("group")
version = property("version")

repositories {
    mavenCentral()
    maven("https://harleyoconnor.com/maven/")
}

dependencies {
    implementation(group = "com.google.guava", name = "guava", version = property("guavaVersion") + "-jre")
    implementation(group = "com.harleyoconnor.javautilities", name = "JavaUtilities", version = property("javaUtilitiesVersion"))

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = property("junitVersion"))
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine")
}

tasks.test {
    this.useJUnitPlatform()
}

java {
    this.withJavadocJar()
    this.withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            this.artifactId = projectName

            this.from(components["java"])

            pom {
                this.name.set(projectName)
                this.url.set("https://github.com/Harleyoc1/${projectName}")
                licenses {
                    license {
                        this.name.set("MIT")
                        this.url.set("https://mit-license.org")
                    }
                }
                developers {
                    developer {
                        this.id.set("harleyoconnor")
                        this.name.set("Harley O\"Connor")
                        this.email.set("harleyoc1@gmail.com")
                    }
                }
                scm {
                    this.connection.set("scm:git:git://github.com/Harleyoc1/${projectName}.git")
                    this.developerConnection.set("scm:git:ssh://github.com/Harleyoc1/${projectName}.git")
                    this.url.set("https://github.com/Harleyoc1/${projectName}")
                }
            }
        }
    }
}