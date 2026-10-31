package dev.darcosse.common.justenoughcobblemon.compat.jade.provider

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.block.PastureBlock
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeSupport
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeUids
import dev.darcosse.common.justenoughcobblemon.config.DisplayMode
import dev.darcosse.common.justenoughcobblemon.config.JecConfig
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.theme.IThemeHelper

/**
 * Pasture block details: owner, how many Pokémon are pastured out of the limit, and which ones
 * (by default only while sneaking).
 *
 * Server data only: the pasture never sends its tethered Pokémon to the client, so nothing is
 * shown when the server lacks Jade or this mod, or turned pastures off in server.json.
 *
 * @author Darcosse
 * @version 1.2
 * @since 2026
 */
object PastureProvider : IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    /**
     * Payload format version. Increment whenever a key below is added, removed or changes type,
     * and keep [appendServerData] and [appendTooltip] in sync.
     */
    private const val PAYLOAD_VERSION = 1

    private const val KEY_OWNER = "o"
    private const val KEY_COUNT = "c"
    private const val KEY_LIMIT = "l"

    /** Listed Pokémon: compounds holding species id, level and shiny flag. */
    private const val KEY_LIST = "p"
    private const val KEY_SPECIES = "s"
    private const val KEY_LEVEL = "v"
    private const val KEY_SHINY = "h"

    /** Marker appended to shiny Pokémon. */
    private const val SHINY_MARKER = " ★"

    /** Reuses the spawn screen's "... and %s more" string instead of a duplicate key. */
    private const val AND_MORE_KEY = "justenoughcobblemon.ui.spawn.and_more"

    /** Adds owner, count and the list of pastured Pokémon, each according to its display mode. */
    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val payload = JadeSupport.readPayload(accessor, JadeUids.PASTURE, PAYLOAD_VERSION) ?: return
        val modes = JecConfig.client().jade.pasture

        val owner = payload.getString(KEY_OWNER)
        if (owner.isNotEmpty() && JadeSupport.visible(modes.owner, accessor)) {
            tooltip.add(JadeSupport.text("pasture.owner", owner))
        }

        val count = payload.getInt(KEY_COUNT)
        if (JadeSupport.visible(modes.count, accessor)) {
            tooltip.add(JadeSupport.text("pasture.count", count, payload.getInt(KEY_LIMIT)))
        }

        if (!JadeSupport.visible(modes.pokemonList, accessor)) return

        val listed = payload.getList(KEY_LIST, Tag.TAG_COMPOUND.toInt())
        listed.forEach { element ->
            val entry = element as CompoundTag
            val line = speciesName(entry.getString(KEY_SPECIES)).copy()
                .append(" ")
                .append(JadeSupport.text("pasture.level", entry.getInt(KEY_LEVEL)))
            if (entry.getBoolean(KEY_SHINY)) {
                line.append(IThemeHelper.get().warning(SHINY_MARKER))
            }
            tooltip.add(line)
        }
        if (count > listed.size) {
            tooltip.add(Component.translatable(AND_MORE_KEY, count - listed.size))
        }
    }

    /** Translated species name, or the raw id if the client does not know the species. */
    private fun speciesName(id: String): Component {
        // Early returns rather than "?.let { } ... ?: literal": see EvYieldProvider for why a
        // Cobblemon type must not meet another type at a shared bytecode label.
        val identifier = ResourceLocation.tryParse(id) ?: return Component.literal(id)
        val species = PokemonSpecies.getByIdentifier(identifier) ?: return Component.literal(id)
        return species.translatedName
    }

    /**
     * Server side: reads the pasture from its bottom half, which is the only one holding the
     * block entity, so looking at the top half works too.
     *
     * Each listed Pokémon is looked up in its owner's PC; this runs on each Jade data request
     * while a player looks at the pasture, and is capped by `pastureMaxListed` in server.json.
     */
    override fun appendServerData(data: CompoundTag, accessor: BlockAccessor) {
        val settings = JecConfig.server().jade
        if (!settings.pasture) return
        val block = accessor.block as? PastureBlock ?: return
        val basePos = block.getBasePosition(accessor.blockState, accessor.position)
        val pasture = accessor.level.getBlockEntity(basePos) as? PokemonPastureBlockEntity ?: return

        val payload = CompoundTag()
        payload.putString(KEY_OWNER, pasture.ownerName)
        payload.putInt(KEY_COUNT, pasture.tetheredPokemon.size)
        payload.putInt(KEY_LIMIT, pasture.getMaxTethered())

        val list = ListTag()
        pasture.tetheredPokemon.asSequence()
            .mapNotNull { it.getPokemon() }
            .take(settings.pastureMaxListedClamped())
            .forEach { pokemon ->
                list.add(CompoundTag().apply {
                    putString(KEY_SPECIES, pokemon.species.resourceIdentifier.toString())
                    putInt(KEY_LEVEL, pokemon.level)
                    putBoolean(KEY_SHINY, pokemon.shiny)
                })
            }
        payload.put(KEY_LIST, list)

        JadeSupport.writePayload(data, JadeUids.PASTURE, PAYLOAD_VERSION, payload)
    }

    /** Skips the request when the player set every pasture line to "never". */
    override fun shouldRequestData(accessor: BlockAccessor): Boolean {
        val modes = JecConfig.client().jade.pasture
        return listOf(modes.owner, modes.count, modes.pokemonList).any { it != DisplayMode.NEVER }
    }

    /** Always on in Jade: visibility is decided per line by client.json, not by Jade's settings. */
    override fun isRequired(): Boolean = true

    /** Provider id. */
    override fun getUid(): ResourceLocation = JadeUids.PASTURE
}
