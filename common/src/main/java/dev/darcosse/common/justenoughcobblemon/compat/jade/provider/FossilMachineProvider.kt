package dev.darcosse.common.justenoughcobblemon.compat.jade.provider

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.block.entity.FossilMultiblockEntity
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockStructure
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeSupport
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeUids
import dev.darcosse.common.justenoughcobblemon.config.DisplayMode
import dev.darcosse.common.justenoughcobblemon.config.JecConfig
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.JadeIds
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.theme.IThemeHelper

/**
 * Fossil machine details, shown on any of its three blocks (analyzer, restoration tank,
 * monitor): the fossil inside, the Pokémon it will become, then organic matter, restoration
 * progress or "ready", depending on the machine's state.
 *
 * Server data only: the structure state lives on the analyzer's block entity and is not reliably
 * up to date on the client. Nothing is shown for blocks that are not part of a built machine.
 *
 * @author Darcosse
 * @version 1.2
 * @since 2026
 */
object FossilMachineProvider : IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    /**
     * Payload format version. Increment whenever a key below is added, removed or changes type,
     * and keep [appendServerData] and [appendTooltip] in sync.
     * 2: the fossil is sent as its item ids instead of the fossil data id.
     */
    private const val PAYLOAD_VERSION = 2

    /** Item ids of the fossils inside (list of String). Empty when the machine holds none. */
    private const val KEY_FOSSIL_ITEMS = "f"

    /** Species the fossil restores into, as written in the fossil data (String). */
    private const val KEY_SPECIES = "s"

    /** Organic matter inside (Int). */
    private const val KEY_ORGANIC = "o"

    /** Ticks left on the restoration (Int); not positive when the machine is not running. */
    private const val KEY_TICKS_LEFT = "t"

    /** Whether a restored Pokémon waits in the tank (Boolean). */
    private const val KEY_READY = "r"

    /** Namespace Cobblemon assumes when fossil data names a species without one. */
    private const val DEFAULT_SPECIES_NAMESPACE = "cobblemon"

    /** Separator between the names of a fossil made of several items. */
    private const val FOSSIL_NAME_SEPARATOR = " + "

    /** Adds fossil, result, then whichever of ready / restoring / organic matter applies. */
    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val payload = JadeSupport.readPayload(accessor, JadeUids.FOSSIL_MACHINE, PAYLOAD_VERSION) ?: return
        val modes = JecConfig.client().jade.fossilMachine
        val theme = IThemeHelper.get()

        if (JadeSupport.visible(modes.fossil, accessor)) {
            val fossilName = fossilName(payload.getList(KEY_FOSSIL_ITEMS, Tag.TAG_STRING.toInt()))
            if (fossilName != null) {
                // Jade's item grid lists the same fossil items; our line names them already.
                tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE)
                tooltip.add(JadeSupport.text("fossil_machine.fossil", fossilName))
            }
        }

        if (payload.contains(KEY_SPECIES) && JadeSupport.visible(modes.result, accessor)) {
            tooltip.add(JadeSupport.text("fossil_machine.result", speciesName(payload.getString(KEY_SPECIES))))
        }

        if (!JadeSupport.visible(modes.progress, accessor)) return
        val ticksLeft = payload.getInt(KEY_TICKS_LEFT)
        when {
            payload.getBoolean(KEY_READY) ->
                tooltip.add(theme.success(JadeSupport.text("fossil_machine.ready")))

            ticksLeft > 0 -> {
                val done = 1F - ticksLeft.toFloat() / FossilMultiblockStructure.TIME_TO_TAKE
                val duration = JadeSupport.plain(theme.seconds(ticksLeft, accessor.tickRate()))
                tooltip.add(JadeSupport.bar(done, JadeSupport.text("fossil_machine.restoring", duration), false))
            }

            else -> {
                val organic = payload.getInt(KEY_ORGANIC)
                val needed = FossilMultiblockStructure.MATERIAL_TO_START
                tooltip.add(JadeSupport.bar(organic.toFloat() / needed, JadeSupport.text("fossil_machine.organic", organic, needed), true))
            }
        }
    }

    /**
     * Name of the fossil inside, built from its items. Cobblemon's own fossil names
     * (`Fossil.getName()`) have no translation in 1.8.4, while the items do.
     */
    private fun fossilName(itemIds: ListTag): Component? {
        val names = itemIds.mapNotNull { tag ->
            ResourceLocation.tryParse(tag.asString)
                ?.let { BuiltInRegistries.ITEM.getOptional(it).orElse(null) }
                ?.description
        }
        if (names.isEmpty()) return null

        val joined = Component.empty()
        names.forEachIndexed { index, name ->
            if (index > 0) joined.append(FOSSIL_NAME_SEPARATOR)
            joined.append(name)
        }
        return joined
    }

    /** Species name from fossil data, which may omit the namespace. */
    private fun speciesName(raw: String): Component {
        // Early returns rather than "?.let { } ... ?: literal": see EvYieldProvider for why a
        // Cobblemon type must not meet another type at a shared bytecode label.
        val id = (if (raw.contains(':')) ResourceLocation.tryParse(raw) else ResourceLocation.tryBuild(DEFAULT_SPECIES_NAMESPACE, raw))
            ?: return Component.literal(raw)
        val species = PokemonSpecies.getByIdentifier(id) ?: return Component.literal(raw)
        return species.translatedName
    }

    /**
     * Server side. Every block of the machine resolves the shared structure through its block
     * entity, so the three blocks show the same thing.
     */
    override fun appendServerData(data: CompoundTag, accessor: BlockAccessor) {
        if (!JecConfig.server().jade.fossilMachine) return
        val entity = accessor.blockEntity as? FossilMultiblockEntity ?: return
        val structure = entity.multiblockStructure as? FossilMultiblockStructure ?: return

        val payload = CompoundTag()
        val fossilItems = ListTag()
        structure.fossilInventory
            .filter { !it.isEmpty }
            .forEach { fossilItems.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(it.item).toString())) }
        payload.put(KEY_FOSSIL_ITEMS, fossilItems)

        // Nested ifs rather than a "?." chain, for the same reason as in speciesName.
        val fossil = structure.resultingFossil
        if (fossil != null) {
            val species = fossil.result.species
            if (species != null) payload.putString(KEY_SPECIES, species)
        }
        payload.putInt(KEY_ORGANIC, structure.organicMaterialInside)
        payload.putInt(KEY_TICKS_LEFT, structure.timeRemaining)
        payload.putBoolean(KEY_READY, structure.hasCreatedPokemon)

        JadeSupport.writePayload(data, JadeUids.FOSSIL_MACHINE, PAYLOAD_VERSION, payload)
    }

    /** Skips the request when the player set every fossil machine line to "never". */
    override fun shouldRequestData(accessor: BlockAccessor): Boolean {
        val modes = JecConfig.client().jade.fossilMachine
        return listOf(modes.fossil, modes.result, modes.progress).any { it != DisplayMode.NEVER }
    }

    /**
     * Runs after Jade's item storage line so the fossil line can replace it; see
     * [JadeSupport.ITEM_STORAGE_PRIORITY].
     */
    override fun getDefaultPriority(): Int = JadeSupport.ITEM_STORAGE_PRIORITY + 1

    /** Always on in Jade: visibility is decided per line by client.json, not by Jade's settings. */
    override fun isRequired(): Boolean = true

    /** Provider id. */
    override fun getUid(): ResourceLocation = JadeUids.FOSSIL_MACHINE
}
