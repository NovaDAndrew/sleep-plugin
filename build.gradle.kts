plugins {
    java
    id("de.eldoria.plugin-yml.bukkit") version "0.9.0"
}

group = "com.sleapplugin"
version = "1.0.4"
description = "A plugin that allows night skip with half of online players"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("net.luckperms:api:5.4")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

bukkit {
    main = "com.sleapplugin.SleepPlugin"
    name = "SleepPlugin"
    version = "1.0.4"
    description = "A plugin that allows night skip with half of online players"
    apiVersion = "26.1"
    author = "NovaDAndrew" //Puer33 minecraft nickname
    softDepend = listOf("LuckPerms")
    
    permissions {
        register("sleepplugin.admin") {
            description = "Allows access to sleep plugin admin commands"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("sleepplugin.exempt") {
            description = "Player is not counted for sleep percentage calculations"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.FALSE
        }
        register("sleepplugin.bypass.min-players") {
            description = "Allows night skip even when below min-players-required"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.FALSE
        }
    }

    commands {
        register("sleep") {
            description = "SleepPlugin admin commands"
            usage = "/sleep <reload|status>"
            permission = "sleepplugin.admin"
        }
    }
}

tasks {
    jar {
        archiveBaseName.set("SleepPlugin")
        archiveVersion.set(project.version.toString())
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "NovaDAndrew"
            )
        }
    }
}
