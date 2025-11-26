plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.sleapplugin"
version = rootProject.version
description = "Sleep plugin for Bukkit/Spigot/Paper/Purpur"

repositories {
    mavenCentral()
    maven { name = "papermc"; url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { name = "spigotmc"; url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
}

dependencies {
    implementation(project(":core"))
    compileOnly("org.spigotmc:spigot-api:1.21.5-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

bukkit {
    main = "com.sleapplugin.SleepPlugin"
    name = "SleepPlugin"
    version = rootProject.version.toString()
    description = "A plugin that allows night skip with half of online players"
    apiVersion = "1.21"
    author = "NovaDAndrew"

    permissions {
        register("sleepplugin.admin") {
            description = "Allows access to sleep plugin admin commands"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "NovaDAndrew"
        )
    }
    archiveFileName.set("SleepPlugin-${project.version}-bukkit.jar")
}
