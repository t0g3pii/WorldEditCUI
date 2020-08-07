plugins {
    java
    id("fabric-loom") version "0.4-SNAPSHOT"
}

val minecraftVersion = "1.16.2-rc1"
val yarnVersion = "$minecraftVersion+build.1:v2"
val fabricLoaderVersion = "0.9.0+build.204"
val fabricApiVersion = "0.16.4+build.392-1.16"
val modmenuVersion = "1.14.6+build.31"

group = "com.mumfrey.worldeditcui"
version = "$minecraftVersion+01"

repositories {
    maven(url = "https://maven.enginehub.org/repo") {
        name = "enginehub"
    }
    maven(url = "https://maven.dblsaiko.net/") {
        name = "dblsaiko"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnVersion")
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modImplementation("io.github.prospector:modmenu:$modmenuVersion")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2") // compiler will crash without?

    modImplementation("grondag:frex-mc116:3.1+") // for render event

    // for development
    /*modRuntime("com.sk89q.worldedit:worldedit-fabric-mc$minecraftVersion:7.2.0-SNAPSHOT") {
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
        exclude(group = "it.unimi.dsi")
        exclude(group = "org.apache.logging.log4j")
    }*/
}

tasks.processResources.configure {
    inputs.property("version", project.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
        expand("version" to project.version)
    }
}
