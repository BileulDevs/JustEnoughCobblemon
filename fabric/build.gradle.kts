plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    enableTransitiveAccessWideners.set(true)
    silentMojangMappingsLicense()
    accessWidenerPath.set(file("../common/src/main/resources/justenoughcobblemon.accesswidener"))

    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
}

val shadowCommon: Configuration by configurations.creating

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    // Loader & API
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    modImplementation(fabricApi.module("fabric-command-api-v2", property("fabric_api_version").toString()))

    // Cobblemon & Kotlin (Nécessaire pour l'interop)
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin")}")
    modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}") { isTransitive = false }

    // Architectury : Liaison avec le module Common
    implementation(project(":common", configuration = "namedElements"))
    "developmentFabric"(project(":common", configuration = "namedElements"))
    shadowCommon(project(":common", configuration = "transformProductionFabric"))

    // JEI : On utilise une seule déclaration propre
    // modApi permet au module fabric de voir les classes JEI utilisées dans le common
    modApi("mezz.jei:jei-1.21.1-fabric:19.27.0.340") {
        isTransitive = false
    }

    annotationProcessor("mezz.jei:jei-1.21.1-fabric:19.27.0.340")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit_version")}")
}

tasks {
    test {
        useJUnitPlatform()
    }

    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(project.properties)
        }
    }

    jar {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-${project.name}")
        archiveClassifier.set("dev-slim")
    }

    shadowJar {
        archiveClassifier.set("dev-shadow")
        archiveBaseName.set("${rootProject.property("archives_base_name")}-${project.name}")
        configurations = listOf(shadowCommon)

        // Optionnel : évite les conflits de métadonnées
        exclude("architectury.common.json")
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set("${rootProject.property("archives_base_name")}-${project.name}")
        archiveVersion.set("${rootProject.version}")
    }
}