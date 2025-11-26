plugins {
    java
}

group = "com.sleapplugin"
version = "1.0.4"
description = "Sleep plugin multi-platform workspace"

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven { name = "papermc"; url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { name = "spigotmc"; url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { name = "sonatype"; url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { name = "sonatype-central"; url = uri("https://oss.sonatype.org/content/repositories/central") }
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}
