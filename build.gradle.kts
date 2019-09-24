plugins {
    java
    id("fabric-loom") version "0.2.4-SNAPSHOT"
}

group = "com.mumfrey.worldeditcui"
version = "1.14.4_03"

val minecraftVersion = "1.14.4"
val yarnVersion = "1.14.4+build.12"
val fabricLoaderVersion = "0.6.2+build.166"
val fabricApiVersion = "0.3.3+build.234-1.14"
val modmenuVersion = "1.7.9+build.118"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    add("minecraft", "com.mojang:minecraft:$minecraftVersion")
    add("mappings", "net.fabricmc:yarn:$yarnVersion")
    add("modCompile", "net.fabricmc:fabric-loader:$fabricLoaderVersion")
    add("modCompile", "net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    add("modCompile", "io.github.prospector:modmenu:$modmenuVersion")
}

val processResources by tasks.getting(ProcessResources::class) {
    inputs.property("version", project.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
        expand("version" to project.version)
    }
}
