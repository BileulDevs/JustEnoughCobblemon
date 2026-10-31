plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

architectury {
    common("neoforge", "fabric")
}

loom {
    silentMojangMappingsLicense()
    accessWidenerPath.set(file("src/main/resources/justenoughcobblemon.accesswidener"))
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("com.cobblemon:mod:${property("cobblemon_version")}") { isTransitive = false }

    // Common code must only see the loader-neutral API; the Fabric API jar would let Fabric-only
    // classes compile here and then fail at runtime on NeoForge.
    compileOnly("mezz.jei:jei-1.21.1-common-api:19.27.0.340")

    // Jade is optional: compiled against, never bundled. Jade has no loader-neutral artifact, but
    // its API package is identical on Fabric and NeoForge for 1.21.1, so common compiles against
    // the Fabric jar (remapped by Loom, hence modCompileOnly). Only the compat.jade package may
    // import it.
    modCompileOnly("maven.modrinth:jade:${property("jade_version")}+fabric")

    compileOnly("org.spongepowered:mixin:0.8.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit_version")}")
}

tasks.test {
    useJUnitPlatform()
}