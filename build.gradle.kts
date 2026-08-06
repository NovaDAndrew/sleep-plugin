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
    
    permissions {
        register("sleepplugin.admin") {
            description = "Allows access to sleep plugin admin commands"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
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
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "NovaDAndrew"
            )
        }
    }
}
