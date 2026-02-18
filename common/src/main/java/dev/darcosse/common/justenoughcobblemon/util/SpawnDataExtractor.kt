package dev.darcosse.common.justenoughcobblemon.util

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.MoonPhaseRange
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.registry.BiomeIdentifierCondition
import com.cobblemon.mod.common.registry.BiomeTagCondition
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.biome.Biome

/**
 * Data class representing detailed spawn information for a Pokémon.
 * Includes support for translatable UI components and regional forms.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
data class SpawnInfo(
    val bucket: String,
    val weight: Float,
    val levelRange: IntRange?,
    val form: String?,
    val biomes: List<String>,
    val anticonditionBiomes: List<String>,
    val structures: List<String>,
    val dimensions: List<String>,
    val minY: Float?,
    val maxY: Float?,
    val minLight: Int?,
    val maxLight: Int?,
    val minSkyLight: Int?,
    val maxSkyLight: Int?,
    val timeRange: String?,
    val moonPhase: String?,
    val canSeeSky: Boolean?,
    val isRaining: Boolean?,
    val isThundering: Boolean?,
    val isSlimeChunk: Boolean?,
    val markers: List<String>,
    val labels: List<String>
) {
    /**
     * Converts spawn data into a list of formatted and translated strings for display.
     */
    fun toDisplayLines(): List<String> {
        val lines = mutableListOf<String>()

        lines.add("§l§6${tr("bucket")}:§r $bucket")
        lines.add("§l§6${tr("weight")}:§r ${"%.2f".format(weight)}")

        levelRange?.let {
            lines.add("§l§6${tr("level")}:§r ${it.first} - ${it.last}")
        }

        form?.let {
            lines.add("§l§6${tr("form")}:§r $it")
        }

        if (biomes.isNotEmpty()) {
            lines.add("§l§6${tr("biomes")}:§r")
            biomes.forEach { lines.add("  §7$it") }
        }

        if (anticonditionBiomes.isNotEmpty()) {
            lines.add("§l§6${tr("excluded_biomes")}:§r")
            anticonditionBiomes.forEach { lines.add("  §7$it") }
        }

        if (structures.isNotEmpty()) {
            lines.add("§l§6${tr("structures")}:§r")
            structures.forEach { lines.add("  §7$it") }
        }

        if (dimensions.isNotEmpty()) {
            lines.add("§l§6${tr("dimensions")}:§r")
            dimensions.forEach { lines.add("  §7$it") }
        }

        if (minY != null || maxY != null) {
            lines.add("§l§6${tr("y_range")}:§r ${minY ?: "min"} - ${maxY ?: "max"}")
        }

        if (minLight != null || maxLight != null) {
            lines.add("§l§6${tr("light")}:§r ${minLight ?: "0"} - ${maxLight ?: "15"}")
        }

        if (minSkyLight != null || maxSkyLight != null) {
            lines.add("§l§6${tr("sky_light")}:§r ${minSkyLight ?: "0"} - ${maxSkyLight ?: "15"}")
        }

        timeRange?.let { lines.add("§l§6${tr("time")}:§r $it") }
        moonPhase?.let { lines.add("§l§6${tr("moon_phase")}:§r $it") }
        canSeeSky?.let { lines.add("§l§6${tr("sees_sky")}:§r $it") }
        isRaining?.let { lines.add("§l§6${tr("raining")}:§r $it") }
        isThundering?.let { lines.add("§l§6${tr("thundering")}:§r $it") }
        isSlimeChunk?.let { if (it) lines.add("§l§6${tr("slime_chunk")}:§r true") }

        if (markers.isNotEmpty()) {
            lines.add("§l§6${tr("markers")}:§r ${markers.joinToString(", ")}")
        }

        if (labels.isNotEmpty()) {
            lines.add("§l§6${tr("labels")}:§r ${labels.joinToString(", ")}")
        }

        return lines
    }

    /**
     * Helper function to translate spawn UI keys.
     */
    private fun tr(key: String): String =
        Component.translatable("justenoughcobblemon.ui.spawn.$key").string
}

/**
 * Utility object to extract and format spawn data from Cobblemon's world spawn pool.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
object SpawnDataExtractor {

    /**
     * Retrieves all spawn details for a specific Pokémon species.
     */
    fun getSpawnsForSpecies(speciesId: ResourceLocation): List<SpawnInfo> {
        return CobblemonSpawnPools.WORLD_SPAWN_POOL.details
            .filterIsInstance<PokemonSpawnDetail>()
            .filter { detail ->
                val detailSpeciesId = detail.pokemon.species ?: return@filter false
                detailSpeciesId == speciesId.path
            }
            .map { extractInfo(it) }
    }

    /**
     * Extracts raw PokemonSpawnDetail into a structured SpawnInfo object,
     * including regional aspect detection.
     */
    private fun extractInfo(detail: PokemonSpawnDetail): SpawnInfo {
        val conditions = detail.conditions
        val anticonditions = detail.anticonditions

        return SpawnInfo(
            bucket = detail.bucket.name,
            weight = detail.weight,
            levelRange = detail.levelRange,
            form = detail.pokemon.aspects
                .firstOrNull { it.isNotBlank() }
                ?.replaceFirstChar { it.uppercase() },

            biomes = conditions.flatMap { condition ->
                condition.biomes?.map { formatBiomeCondition(it) } ?: emptyList()
            }.distinct(),

            anticonditionBiomes = anticonditions.flatMap { condition ->
                condition.biomes?.map { formatBiomeCondition(it) } ?: emptyList()
            }.distinct(),

            structures = conditions.flatMap { condition ->
                condition.structures?.map { either ->
                    either.map({ formatId(it.toString()) }, { "#${formatId(it.location().toString())}" })
                } ?: emptyList()
            }.distinct(),

            dimensions = conditions.flatMap { condition ->
                condition.dimensions?.map { formatId(it.toString()) } ?: emptyList()
            }.distinct(),

            minY = conditions.firstNotNullOfOrNull { it.minY },
            maxY = conditions.firstNotNullOfOrNull { it.maxY },
            minLight = conditions.firstNotNullOfOrNull { it.minLight },
            maxLight = conditions.firstNotNullOfOrNull { it.maxLight },
            minSkyLight = conditions.firstNotNullOfOrNull { it.minSkyLight },
            maxSkyLight = conditions.firstNotNullOfOrNull { it.maxSkyLight },

            timeRange = conditions.firstNotNullOfOrNull { it.timeRange }?.let { formatTimeRange(it) },
            moonPhase = conditions.firstNotNullOfOrNull { it.moonPhase }?.let { formatMoonPhase(it) },
            canSeeSky = conditions.firstNotNullOfOrNull { it.canSeeSky },
            isRaining = conditions.firstNotNullOfOrNull { it.isRaining },
            isThundering = conditions.firstNotNullOfOrNull { it.isThundering },
            isSlimeChunk = conditions.firstNotNullOfOrNull { it.isSlimeChunk },

            markers = conditions.flatMap { it.markers ?: emptyList() }.distinct(),
            labels = detail.labels.toList()
        )
    }

    /**
     * Formats biome conditions (ID or Tag) into a readable string.
     */
    private fun formatBiomeCondition(biome: RegistryLikeCondition<Biome>): String {
        return when (biome) {
            is BiomeIdentifierCondition -> formatId(biome.identifier.toString())
            is BiomeTagCondition -> "#${formatId(biome.tag.location().toString())}"
            else -> biome.toString()
        }
    }

    /**
     * Cleans up ResourceLocation strings for better readability.
     */
    private fun formatId(id: String): String {
        return try {
            val rl = ResourceLocation.parse(id)
            if (rl.namespace == "minecraft") {
                rl.path.replace("_", " ").replaceFirstChar { it.uppercase() }
            } else {
                "[${rl.namespace}] ${rl.path.replace("_", " ").replaceFirstChar { it.uppercase() }}"
            }
        } catch (e: Exception) { id }
    }

    /**
     * Accesses the 'ranges' field via reflection to get raw numerical range data.
     */
    private fun getRangesField(obj: Any): List<IntRange> {
        return try {
            val field = obj::class.java.superclass.getDeclaredField("ranges")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            field.get(obj) as List<IntRange>
        } catch (e: Exception) { emptyList() }
    }

    /**
     * Formats time ranges into named constants (e.g., "Day") or raw values.
     */
    private fun formatTimeRange(range: TimeRange): String {
        val rangeRanges = getRangesField(range)
        val known = TimeRange.timeRanges.entries.firstOrNull { (_, v) ->
            getRangesField(v) == rangeRanges
        }
        return known?.key?.replaceFirstChar { it.uppercase() } ?: rangeRanges.joinToString(", ") { "${it.first}..${it.last}" }
    }

    /**
     * Formats moon phases into named constants or raw values.
     */
    private fun formatMoonPhase(range: MoonPhaseRange): String {
        val rangeRanges = getRangesField(range)
        val known = MoonPhaseRange.moonPhaseRanges.entries.firstOrNull { (_, v) ->
            getRangesField(v) == rangeRanges
        }
        return known?.key?.replaceFirstChar { it.uppercase() } ?: rangeRanges.joinToString(", ") { "${it.first}..${it.last}" }
    }
}