plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

architectury {
    common("neoforge", "fabric")
}

loom {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("com.cobblemon:mod:${property("cobblemon_version")}") { isTransitive = false }

    compileOnly("mezz.jei:jei-1.21.1-common-api:19.27.0.340")
    compileOnly("mezz.jei:jei-1.21.1-fabric-api:19.27.0.340")

    compileOnly("org.spongepowered:mixin:0.8.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit_version")}")
}

tasks.test {
    useJUnitPlatform()
}