plugins {
    java
    id("fabric-loom") version "0.2.4-SNAPSHOT"
}

group = "com.mumfrey.worldeditcui"
version = "1.14.4_02"

val minecraftVersion = "1.14.4"
val yarnVersion = "1.14.4+build.2"
val fabricLoaderVersion = "0.4.8+build.158"
val fabricApiVersion = "0.3.0+build.200"
val modmenuVersion = "1.6.2-92"

dependencies {
    add("minecraft", "com.mojang:minecraft:$minecraftVersion")
    add("mappings", "net.fabricmc:yarn:$yarnVersion")
    add("modCompile", "net.fabricmc:fabric-loader:$fabricLoaderVersion")
    add("modCompile", "net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    add("modCompile", "io.github.prospector.modmenu:ModMenu:$modmenuVersion")
}

val processResources by tasks.getting(ProcessResources::class) {
    inputs.property("version", project.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
        expand("version" to project.version)
    }
}