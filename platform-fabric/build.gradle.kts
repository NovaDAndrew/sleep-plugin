plugins {
    java
}

group = "com.sleapplugin"
version = rootProject.version
description = "Sleep plugin core integration for Fabric"

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
    archiveFileName.set("SleepPlugin-${project.version}-fabric.jar")
}
