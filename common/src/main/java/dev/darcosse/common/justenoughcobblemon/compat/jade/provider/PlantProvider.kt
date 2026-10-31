package dev.darcosse.common.justenoughcobblemon.compat.jade.provider

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.block.GrowableStoneBlock
import com.cobblemon.mod.common.block.RevivalHerbBlock
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeSupport
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeUids
import dev.darcosse.common.justenoughcobblemon.config.JecConfig
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.theme.IThemeHelper
import java.util.Locale

/**
 * Growth information Jade does not show on its own for Cobblemon plants.
 *
 * Mints, Medicinal Leeks, Revival Herbs, apricorns and berry trees already get Jade's native
 * "Growth" line, because they are crops or bonemealable blocks with a standard age property.
 * This provider covers the rest:
 * - growable stones (Tumblestones): growth percentage, and a warning when they cannot grow where
 *   they are;
 * - Revival Herbs: the herb a mutation will drop, or that a mutation is still possible.
 *
 * Client only: everything comes from the block state.
 *
 * @author Darcosse
 * @version 1.2
 * @since 2026
 */
object PlantProvider : IBlockComponentProvider {

    /**
     * Jade's own growth strings, reused so the stone line looks exactly like the native crop
     * line. They belong to Jade, so they exist whenever this provider runs.
     */
    private const val JADE_GROWTH_KEY = "tooltip.jade.crop_growth"
    private const val JADE_MATURE_KEY = "tooltip.jade.crop_mature"

    /**
     * Safety bound when walking a stone's growth chain. Cobblemon's chains have four stages; the
     * bound only protects against a data pack creating a loop.
     */
    private const val MAX_CHAIN_LENGTH = 16

    /** Dispatches on the plant type, each following its own display mode. */
    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val modes = JecConfig.client().jade.plants
        when (val block = accessor.block) {
            is GrowableStoneBlock -> if (JadeSupport.visible(modes.stoneGrowth, accessor)) appendStoneGrowth(tooltip, accessor, block)
            is RevivalHerbBlock -> if (JadeSupport.visible(modes.herbMutation, accessor)) appendMutation(tooltip, accessor, block)
        }
    }

    /**
     * Growth of a stone, computed from its position in the chain of next stages, since each stage
     * is a separate block rather than an age property.
     */
    private fun appendStoneGrowth(tooltip: ITooltip, accessor: BlockAccessor, block: GrowableStoneBlock) {
        val theme = IThemeHelper.get()

        var remaining = 0
        var next = block.nextStage
        while (next != null && remaining < MAX_CHAIN_LENGTH) {
            remaining++
            // Not "(next as? GrowableStoneBlock)?.nextStage": see EvYieldProvider for why a
            // Cobblemon type must not meet another type at a shared bytecode label.
            next = if (next is GrowableStoneBlock) next.nextStage else null
        }

        if (remaining == 0) {
            tooltip.add(Component.translatable(JADE_GROWTH_KEY, theme.success(Component.translatable(JADE_MATURE_KEY))))
            return
        }

        val total = block.stage + remaining
        val percent = String.format(Locale.ROOT, "%.0f%%", block.stage * 100F / total)
        tooltip.add(Component.translatable(JADE_GROWTH_KEY, theme.info(percent)))

        if (!block.canGrow(accessor.blockState, accessor.position, accessor.level)) {
            tooltip.add(theme.warning(JadeSupport.text("plants.cannot_grow")))
        }
    }

    /**
     * Mutation of a Revival Herb. The herb dropped per mutation comes from Cobblemon's
     * revival_herb loot table (mental, mirror, power, white).
     */
    private fun appendMutation(tooltip: ITooltip, accessor: BlockAccessor, block: RevivalHerbBlock) {
        val state = accessor.blockState
        val herb: Item? = when (state.getValue(RevivalHerbBlock.MUTATION)) {
            RevivalHerbBlock.Mutation.MENTAL -> CobblemonItems.MENTAL_HERB
            RevivalHerbBlock.Mutation.MIRROR -> CobblemonItems.MIRROR_HERB
            RevivalHerbBlock.Mutation.POWER -> CobblemonItems.POWER_HERB
            RevivalHerbBlock.Mutation.WHITE -> CobblemonItems.WHITE_HERB
            else -> null
        }

        if (herb != null) {
            tooltip.add(JadeSupport.text("plants.mutation", herb.description))
        } else if (block.getAge(state) <= RevivalHerbBlock.MUTABLE_MAX_AGE) {
            // getAge rather than the AGE property: CropBlock also declares a static AGE, and the
            // herb overrides getAgeProperty to point at its own wider one.
            tooltip.add(JadeSupport.text("plants.can_mutate"))
        }
    }

    /** Always on in Jade: visibility is decided per line by client.json, not by Jade's settings. */
    override fun isRequired(): Boolean = true

    /** Provider id. */
    override fun getUid(): ResourceLocation = JadeUids.PLANTS
}
