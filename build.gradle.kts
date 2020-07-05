plugins {
    java
    id("fabric-loom") version "0.4-SNAPSHOT"
}

group = "com.mumfrey.worldeditcui"
version = "1.16.1_01"

val minecraftVersion = "1.16.1"
val yarnVersion = "$minecraftVersion+build.20:v2"
val fabricLoaderVersion = "0.8.9+build.203"
val fabricApiVersion = "0.14.1+build.372-1.16"
val modmenuVersion = "1.12.2+build.17"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnVersion")
    modCompile("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modCompile("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modCompile("io.github.prospector:modmenu:$modmenuVersion")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2") // compiler will crash without?
}

val processResources by tasks.getting(ProcessResources::class) {
    inputs.property("version", project.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
        expand("version" to project.version)
    }
}
