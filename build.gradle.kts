plugins {
    java
    id("fabric-loom") version "0.5-SNAPSHOT"
    id("com.github.ben-manes.versions") version "0.36.0"
}

val minecraftVersion = "1.16.5"
val yarnVersion = "$minecraftVersion+build.1:v2"
val fabricLoaderVersion = "0.11.0"
val fabricApiVersion = "0.29.3+1.16"
val modmenuVersion = "1.14.13+build.22"
val multiconnectVersion = "1.3.34"

group = "com.mumfrey.worldeditcui"
version = "$minecraftVersion+03-SNAPSHOT"

repositories {
    maven(url = "https://maven.enginehub.org/repo") {
        name = "enginehub"
    }
    maven(url = "https://dl.bintray.com/earthcomputer/mods")
    maven(url = "https://files.minecraftforge.net/maven") {
        name = "forge"
        content { includeGroup("net.minecraftforge") }
    }
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
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
}

dependencies {
    constraints {
        modRuntime("org.yaml:snakeyaml:1.27")
    }

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnVersion")
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modImplementation("io.github.prospector:modmenu:$modmenuVersion")
    modImplementation("net.earthcomputer:multiconnect:$multiconnectVersion:api")

    // for development
    modRuntime("com.sk89q.worldedit:worldedit-fabric-mc1.16.3:7.2.3") {
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
        exclude(group = "it.unimi.dsi")
        exclude(group = "org.apache.logging.log4j")
    }
    runtimeOnly("net.minecraftforge:forgeflower:latest.release")
}

tasks.withType(net.fabricmc.loom.task.AbstractRunTask::class).configureEach {
    // Midxin debug options
    jvmArgs(
        // "-Dmixin.debug.verbose=true",
        // "-Dmixin.debug.export=true",
        // "-Dmixin.debug.export.decompile.async=false", // to get decompiled sources when mixins straight up fail to apply
        "-Dmixin.dumpTargetOnFailure=true",
        "-Dmixin.checks.interfaces=true",
        "-Dwecui.debug.mixinaudit=true"
    )

    // Configure mixin agent
    jvmArgumentProviders += CommandLineArgumentProvider {
        // Resolve the Mixin configuration
        // Java agent: the jar file for mixin
        val mixinJar = configurations.runtimeClasspath.get().resolvedConfiguration
            .getFiles { it.name == "sponge-mixin" && it.group == "net.fabricmc" }
            .firstOrNull()

        if (mixinJar != null) {
            listOf("-javaagent:$mixinJar")
        } else {
            emptyList()
        }
    }
}

tasks.processResources.configure {
    inputs.property("version", project.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
        expand("version" to project.version)
    }
}
