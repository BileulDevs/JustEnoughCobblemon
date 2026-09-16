package dev.darcosse.common.justenoughcobblemon.util

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.MoonPhaseRange
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.api.spawning.condition.AreaTypeSpawningCondition
import com.cobblemon.mod.common.api.spawning.condition.GroundedTypeSpawningCondition
import com.cobblemon.mod.common.api.spawning.condition.SeafloorTypeSpawningCondition
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.registry.BiomeIdentifierCondition
import com.cobblemon.mod.common.registry.BiomeTagCondition
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome

/**
 * One weight multiplier attached to a spawn.
 *
 * WeightMultiplier carries full condition lists, not a single flag: the
 * multiplier applies when at least one condition matches AND no anticondition
 * does. Rather than re-render the whole condition tree inside a tooltip, each
 * side is summarised down to the handful of fields players actually read.
 */
data class MultiplierInfo(
    val multiplier: Float,
    val conditions: List<String>,
    val anticonditions: List<String>,
    /**
     * Whether the multiplier carries any condition at all.
     *
     * Distinct from `conditions.isEmpty()`: a condition can exist and still
     * produce no summary line, because summarizeCondition only covers the
     * fields worth showing. Deciding "always" on an empty summary meant a
     * multiplier gated on isPokeSnack was announced as unconditional — wrong
     * in the one direction that misleads the player.
     */
    val hasAnyCondition: Boolean
)

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
    /** Where the spawn happens: grounded, submerged, fished, seafloor... */
    val spawnablePosition: String?,
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
    val isPokeSnack: Boolean?,
    val minX: Float?,
    val maxX: Float?,
    val minZ: Float?,
    val maxZ: Float?,
    val markers: List<String>,
    val labels: List<String>,
    val multipliers: List<MultiplierInfo>,
    val neededBaseBlocks: List<String>,
    val neededNearbyBlocks: List<String>
) {
    /**
     * Converts spawn data into a list of formatted and translated strings for display.
     */
    fun toDisplayLines(): List<String> {
        val lines = mutableListOf<String>()

        lines.add("§l§6${tr("bucket")}:§r $bucket")

        spawnablePosition?.let {
            lines.add("§l§6${tr("spawnable_position")}:§r $it")
        }

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

        if (neededBaseBlocks.isNotEmpty()) {
            lines.add("§l§6${tr("base_blocks")}:§r")
            addCapped(lines, neededBaseBlocks)
        }

        if (neededNearbyBlocks.isNotEmpty()) {
            lines.add("§l§6${tr("nearby_blocks")}:§r")
            addCapped(lines, neededNearbyBlocks)
        }

        if (dimensions.isNotEmpty()) {
            lines.add("§l§6${tr("dimensions")}:§r")
            dimensions.forEach { lines.add("  §7$it") }
        }

        if (minY != null || maxY != null) {
            lines.add("§l§6${tr("y_range")}:§r ${minY ?: "min"} - ${maxY ?: "max"}")
        }

        if (minX != null || maxX != null) {
            lines.add("§l§6${tr("x_range")}:§r ${minX ?: "min"} - ${maxX ?: "max"}")
        }

        if (minZ != null || maxZ != null) {
            lines.add("§l§6${tr("z_range")}:§r ${minZ ?: "min"} - ${maxZ ?: "max"}")
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
        isPokeSnack?.let { lines.add("§l§6${tr("poke_snack")}:§r $it") }

        if (multipliers.isNotEmpty()) {
            lines.add("§l§6${tr("multipliers")}:§r")

            multipliers.forEach { m ->
                val value = "×${"%.2f".format(m.multiplier)}"

                val summarised = m.conditions.isNotEmpty() || m.anticonditions.isNotEmpty()

                when {
                    !m.hasAnyCondition -> lines.add("  $value §8${tr("multiplier_always")}")

                    summarised -> {
                        lines.add("  $value")
                        m.conditions.forEach { lines.add("      §7$it") }
                        m.anticonditions.forEach { lines.add("      §c${tr("multiplier_unless")} §7$it") }
                    }
                    else -> lines.add("  $value §8${tr("multiplier_conditional")}")
                }
            }
        }

        if (markers.isNotEmpty()) {
            lines.add("§l§6${tr("markers")}:§r ${markers.joinToString(", ")}")
        }

        if (labels.isNotEmpty()) {
            lines.add("§l§6${tr("labels")}:§r ${labels.joinToString(", ")}")
        }

        return lines
    }

    private fun tr(key: String): String =
        Component.translatable("justenoughcobblemon.ui.spawn.$key").string

    /**
     * Appends a block list, stopping after MAX_BLOCKS_SHOWN entries.
     *
     * Some presets carry dozens of blocks — "natural" is the worst offender —
     * and dumping all of them turns the tooltip into a wall nobody reads. The
     * remainder is summarised as a count.
     */
    private fun addCapped(lines: MutableList<String>, values: List<String>) {
        values.take(MAX_BLOCKS_SHOWN).forEach { value ->
            lines.addAll(wrapWithPrefix(value, "  §7", MAX_LINE_CHARS))
        }

        val remaining = values.size - MAX_BLOCKS_SHOWN
        if (remaining > 0) {
            lines.add("  §8${tr("and_more").replace("%s", remaining.toString())}")
        }
    }

    /**
     * Wraps a value onto several lines, repeating the prefix on each one.
     *
     * Legacy formatting codes do not survive a line break inserted by the
     * renderer: the continuation restarts uncoloured and unindented. Breaking
     * the line here instead keeps every fragment styled and aligned, however
     * long a modded ID turns out to be.
     */
    private fun wrapWithPrefix(text: String, prefix: String, max: Int): List<String> {
        if (text.length <= max) return listOf(prefix + text)

        val lines = mutableListOf<String>()
        val line = StringBuilder()
        for (word in text.split(" ")) {
            if (line.isNotEmpty() && line.length + 1 + word.length > max) {
                lines.add(prefix + line)
                line.setLength(0)
            }
            if (line.isNotEmpty()) line.append(' ')
            line.append(word)
        }
        if (line.isNotEmpty()) lines.add(prefix + line)
        return lines
    }

    companion object {
        /** How many blocks to list before collapsing the rest into a count. */
        const val MAX_BLOCKS_SHOWN = 8

        /** Character budget per tooltip line before wrapping it ourselves. */
        const val MAX_LINE_CHARS = 38
    }
}

/**
 * Utility object to extract and format spawn data from Cobblemon's world spawn pool.
 *
 * @author Darcosse
 * @version 1.1
 * @since 2026
 */
object SpawnDataExtractor {

    /**
     * Retrieves all spawn details for a specific Pokémon species from the global extracted pool.
     */
    fun getSpawnsForSpecies(speciesId: ResourceLocation): List<SpawnInfo> {
        return getAllSpawns()[speciesId.path] ?: emptyList()
    }

    /**
     * Extracts and groups all available world spawns by their species ID.
     * Primarily used by the server to prepare network payloads.
     */
    fun getAllSpawns(): Map<String, List<SpawnInfo>> {
        return CobblemonSpawnPools.WORLD_SPAWN_POOL.details
            .filterIsInstance<PokemonSpawnDetail>()
            .groupBy { it.pokemon.species ?: "" }
            .filter { it.key.isNotBlank() }
            .mapValues { (_, details) -> details.map { extractInfo(it) } }
    }

    /**
     * Extracts raw PokemonSpawnDetail into a structured SpawnInfo object.
     */
    private fun extractInfo(detail: PokemonSpawnDetail): SpawnInfo {
        val conditions = detail.conditions
        val anticonditions = detail.anticonditions

        return SpawnInfo(
            bucket = detail.bucket.toString(),

            spawnablePosition = runCatching {
                formatPositionType(detail.spawnablePositionType.name)
            }.getOrNull(),

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
            isPokeSnack = conditions.firstNotNullOfOrNull { it.isPokeSnack },

            minX = conditions.firstNotNullOfOrNull { it.minX },
            maxX = conditions.firstNotNullOfOrNull { it.maxX },
            minZ = conditions.firstNotNullOfOrNull { it.minZ },
            maxZ = conditions.firstNotNullOfOrNull { it.maxZ },

            markers = conditions.flatMap { it.markers ?: emptyList() }.distinct(),
            labels = detail.labels.toList(),

            neededBaseBlocks = conditions.flatMap { extractBaseBlocks(it) }.distinct(),
            neededNearbyBlocks = conditions.flatMap { extractNearbyBlocks(it) }.distinct(),

            multipliers = detail.weightMultipliers.map { multiplier ->
                MultiplierInfo(
                    multiplier = multiplier.multiplier,
                    conditions = multiplier.conditions.flatMap { summarizeCondition(it) }.distinct(),
                    anticonditions = multiplier.anticonditions.flatMap { summarizeCondition(it) }.distinct(),
                    hasAnyCondition = multiplier.conditions.isNotEmpty() ||
                            multiplier.anticonditions.isNotEmpty()
                )
            }
        )
    }

    /**
     * Base blocks, declared separately on each grounded-style condition.
     *
     * GroundedTypeSpawningCondition and SeafloorTypeSpawningCondition each
     * declare their own neededBaseBlocks rather than sharing one on a common
     * parent, so both branches are needed. Any other condition type simply has
     * no base block.
     */
    private fun extractBaseBlocks(condition: SpawningCondition<*>): List<String> {
        val blocks = when (condition) {
            is GroundedTypeSpawningCondition<*> -> condition.neededBaseBlocks
            is SeafloorTypeSpawningCondition<*> -> condition.neededBaseBlocks
            else -> null
        }
        return blocks?.map { formatBlockCondition(it) } ?: emptyList()
    }

    /**
     * Nearby blocks, declared once on the shared area-based parent.
     */
    private fun extractNearbyBlocks(condition: SpawningCondition<*>): List<String> {
        val blocks = (condition as? AreaTypeSpawningCondition<*>)?.neededNearbyBlocks
        return blocks?.map { formatBlockCondition(it) } ?: emptyList()
    }

    /**
     * Formats a block condition without naming its implementation classes.
     *
     * RegistryLikeCondition has one subclass per registry, and this mod does
     * not need to know which. Reading whichever of "identifier" or "tag" the
     * instance happens to carry keeps this working across Cobblemon versions —
     * the same approach already used for TimeRange below.
     */
    private fun formatBlockCondition(condition: Any): String {
        readField(condition, "identifier")?.let { return formatId(it.toString()) }

        readField(condition, "tag")?.let { tag ->
            if (tag is TagKey<*>) return "#${formatId(tag.location().toString())}"
            return "#${formatId(tagIdFromToString(tag.toString()))}"
        }

        return condition.toString()
    }

    /**
     * Last-resort extraction of a tag ID from a toString() form.
     *
     * Only reached if Cobblemon ever stops exposing a TagKey here. Turns
     * "TagKey[minecraft:block / cobblemon:natural]" into "cobblemon:natural"
     * so the tooltip never shows the raw bracket form.
     */
    private fun tagIdFromToString(raw: String): String =
        TAG_TO_STRING.find(raw)?.groupValues?.get(1) ?: raw

    /** Matches the ID inside "TagKey[minecraft:block / cobblemon:natural]". */
    private val TAG_TO_STRING = Regex("""\[[^\]]*?/\s*([^\]\s]+)]""")

    private fun readField(obj: Any, name: String): Any? = try {
        generateSequence(obj::class.java) { it.superclass }
            .mapNotNull { klass ->
                runCatching { klass.getDeclaredField(name).apply { isAccessible = true }.get(obj) }.getOrNull()
            }
            .firstOrNull()
    } catch (e: Exception) { null }

    /**
     * Reduces a condition to the few lines worth showing under a multiplier.
     *
     * Deliberately partial: a multiplier's condition can carry everything a
     * spawn condition can, but reproducing all of it would bury the number the
     * player came for. Only the fields that meaningfully explain "when does
     * this apply" are kept.
     */
    private fun summarizeCondition(condition: SpawningCondition<*>): List<String> {
        val parts = mutableListOf<String>()

        condition.biomes
            ?.takeIf { it.isNotEmpty() }
            ?.let { parts.add("${tr("biomes")}: ${it.joinToString(", ") { b -> formatBiomeCondition(b) }}") }

        condition.dimensions
            ?.takeIf { it.isNotEmpty() }
            ?.let { parts.add("${tr("dimensions")}: ${it.joinToString(", ") { d -> formatId(d.toString()) }}") }

        condition.structures
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                parts.add("${tr("structures")}: " + it.joinToString(", ") { either ->
                    either.map({ s -> formatId(s.toString()) }, { t -> "#${formatId(t.location().toString())}" })
                })
            }

        condition.timeRange?.let { parts.add("${tr("time")}: ${formatTimeRange(it)}") }
        condition.moonPhase?.let { parts.add("${tr("moon_phase")}: ${formatMoonPhase(it)}") }
        condition.isRaining?.let { parts.add("${tr("raining")}: $it") }
        condition.isThundering?.let { parts.add("${tr("thundering")}: $it") }
        condition.canSeeSky?.let { parts.add("${tr("sees_sky")}: $it") }
        condition.isPokeSnack?.let { parts.add("${tr("poke_snack")}: $it") }
        condition.isSlimeChunk?.let { parts.add("${tr("slime_chunk")}: $it") }

        condition.markers
            ?.takeIf { it.isNotEmpty() }
            ?.let { parts.add("${tr("markers")}: ${it.joinToString(", ")}") }

        val baseBlocks = extractBaseBlocks(condition) + extractNearbyBlocks(condition)
        if (baseBlocks.isNotEmpty()) {
            parts.add("${tr("blocks")}: ${baseBlocks.take(3).joinToString(", ")}" +
                    if (baseBlocks.size > 3) " (+${baseBlocks.size - 3})" else "")
        }

        if (condition.minY != null || condition.maxY != null) {
            parts.add("${tr("y_range")}: ${condition.minY ?: "min"} - ${condition.maxY ?: "max"}")
        }

        if (condition.minLight != null || condition.maxLight != null) {
            parts.add("${tr("light")}: ${condition.minLight ?: 0} - ${condition.maxLight ?: 15}")
        }

        parts.addAll(describeRemainingFields(condition))

        return parts
    }

    /**
     * Describes condition fields this mod does not know about by name.
     *
     * SpawningCondition is subclassed per position type, and every subclass
     * adds its own fields: neededBaseBlocks on grounded and seafloor, lure
     * levels on fishing, and whatever Cobblemon adds next. Handling them with
     * one `is` branch per class meant there was always one more class missing,
     * and the player got "under specific conditions" instead of the real rule.
     *
     * Walking the declared fields covers every subclass at once, including the
     * ones that do not exist yet. Only simple values are rendered; anything
     * structured is skipped rather than dumped as a toString.
     */
    private fun describeRemainingFields(condition: SpawningCondition<*>): List<String> {
        val values = linkedMapOf<String, String>()

        generateSequence(condition::class.java as Class<*>) { it.superclass }
            .takeWhile { it != Any::class.java }
            .flatMap { it.declaredFields.asSequence() }
            .forEach { field ->
                val name = field.name

                if (name in HANDLED_FIELDS) return@forEach
                if (name in values) return@forEach
                if (java.lang.reflect.Modifier.isStatic(field.modifiers)) return@forEach
                if (java.lang.reflect.Modifier.isTransient(field.modifiers)) return@forEach

                val raw = runCatching {
                    field.isAccessible = true
                    field.get(condition)
                }.getOrNull() ?: return@forEach

                renderSimpleValue(raw)?.let { values[name] = it }
            }

        if (values.isEmpty()) return emptyList()

        val parts = mutableListOf<String>()
        val consumed = mutableSetOf<String>()

        values.keys.filter { it.startsWith("min") }.forEach { minKey ->
            val maxKey = "max" + minKey.removePrefix("min")

            values[maxKey]?.let { maxValue ->
                parts.add("${prettifyFieldName(minKey.removePrefix("min"))}: ${values[minKey]} - $maxValue")
                consumed += minKey
                consumed += maxKey
            }
        }

        values.forEach { (name, value) ->
            if (name !in consumed) {
                parts.add("${prettifyFieldName(name)}: $value")
            }
        }

        return parts
    }

    /**
     * @return a readable form for scalars and simple collections, or null for
     *         anything that would render as an object dump.
     */
    private fun renderSimpleValue(value: Any): String? = when (value) {
        is Boolean, is String -> value.toString()
        is Int, is Long, is Short, is Byte -> value.toString()
        is Float -> if (value % 1f == 0f) value.toInt().toString() else "%.2f".format(value)
        is Double -> if (value % 1.0 == 0.0) value.toInt().toString() else "%.2f".format(value)

        is Collection<*> -> value
            .filterNotNull()
            .mapNotNull { element ->
                when (element) {
                    is String -> element
                    is ResourceLocation -> formatId(element.toString())
                    else -> null
                }
            }
            .takeIf { it.isNotEmpty() }
            ?.let { rendered ->
                rendered.take(3).joinToString(", ") +
                        if (rendered.size > 3) " (+${rendered.size - 3})" else ""
            }

        else -> null
    }

    /** "minLureLevel" -> "Lure Level", "isPokeSnack" -> "Poke Snack". */
    private fun prettifyFieldName(name: String): String =
        name.replace(Regex("^(is|has)"), "")
            .replace(Regex("(?<!^)(?=[A-Z])"), " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    /**
     * Fields summarizeCondition renders by hand, so the generic pass does not
     * repeat them.
     */
    private val HANDLED_FIELDS = setOf(
        "biomes", "dimensions", "structures", "timeRange", "moonPhase",
        "isRaining", "isThundering", "canSeeSky", "isPokeSnack", "isSlimeChunk",
        "markers", "minY", "maxY", "minLight", "maxLight",
        "neededBaseBlocks", "neededNearbyBlocks", "appendages"
    )

    /** Shared with SpawnInfo so both sides use the same translated labels. */
    private fun tr(key: String): String =
        Component.translatable("justenoughcobblemon.ui.spawn.$key").string

    /**
     * Turns a spawnable position type name into something readable.
     *
     * Cobblemon uses lowercase identifiers such as "grounded", "submerged",
     * "seafloor" or "fished".
     */
    private fun formatPositionType(name: String): String =
        name.replace("_", " ").replaceFirstChar { it.uppercase() }

    /**
     * Formats biome conditions into readable strings.
     */
    private fun formatBiomeCondition(biome: RegistryLikeCondition<Biome>): String {
        return when (biome) {
            is BiomeIdentifierCondition -> formatId(biome.identifier.toString())
            is BiomeTagCondition -> "#${formatId(biome.tag.location().toString())}"
            else -> biome.toString()
        }
    }

    /**
     * Formats ResourceLocation IDs into human-readable names.
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

    private fun getRangesField(obj: Any): List<IntRange> {
        return try {
            val field = obj::class.java.superclass.getDeclaredField("ranges")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            field.get(obj) as List<IntRange>
        } catch (e: Exception) { emptyList() }
    }

    private fun formatTimeRange(range: TimeRange): String {
        val rangeRanges = getRangesField(range)
        val known = TimeRange.timeRanges.entries.firstOrNull { (_, v) ->
            getRangesField(v) == rangeRanges
        }
        return known?.key?.replaceFirstChar { it.uppercase() } ?: rangeRanges.joinToString(", ") { "${it.first}..${it.last}" }
    }

    private fun formatMoonPhase(range: MoonPhaseRange): String {
        val rangeRanges = getRangesField(range)
        val known = MoonPhaseRange.moonPhaseRanges.entries.firstOrNull { (_, v) ->
            getRangesField(v) == rangeRanges
        }
        return known?.key?.replaceFirstChar { it.uppercase() } ?: rangeRanges.joinToString(", ") { "${it.first}..${it.last}" }
    }
}