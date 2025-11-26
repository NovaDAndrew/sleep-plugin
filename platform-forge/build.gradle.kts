plugins {
    java
}

group = "com.sleapplugin"
version = rootProject.version
description = "Sleep plugin core integration for Forge"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "NovaDAndrew"
        )
    }
}

