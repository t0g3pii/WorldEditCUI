plugins {
    java
    id("fabric-loom") version "0.5-SNAPSHOT"
}

val minecraftVersion = "1.16.4"
val yarnVersion = "$minecraftVersion+build.7:v2"
val fabricLoaderVersion = "0.10.8"
val fabricApiVersion = "0.29.0+1.16"
val modmenuVersion = "1.14.13+build.22"
val multiconnectVersion = "1.3.34"

group = "com.mumfrey.worldeditcui"
version = "$minecraftVersion+01-SNAPSHOT"

repositories {
    maven(url = "https://maven.enginehub.org/repo") {
        name = "enginehub"
    }
    maven(url = "https://dl.bintray.com/earthcomputer/mods")
}

val targetVersion = 8
java {
    sourceCompatibility = JavaVersion.toVersion(targetVersion)
    targetCompatibility = sourceCompatibility
}

tasks.withType(JavaCompile::class) {
    if (JavaVersion.current().isJava10Compatible) {
        options.release.set(targetVersion)
    }
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnVersion")
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modImplementation("io.github.prospector:modmenu:$modmenuVersion")
    modImplementation("net.earthcomputer:multiconnect:$multiconnectVersion:api")

    // for development
    modRuntime("com.sk89q.worldedit:worldedit-fabric-mc1.16.3:7.3.0-SNAPSHOT") {
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
        exclude(group = "it.unimi.dsi")
        exclude(group = "org.apache.logging.log4j")
    }
}

tasks.processResources.configure {
    inputs.property("version", project.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
        expand("version" to project.version)
    }
}
