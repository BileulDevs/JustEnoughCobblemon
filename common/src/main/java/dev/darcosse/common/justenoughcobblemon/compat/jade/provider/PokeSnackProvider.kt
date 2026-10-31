package dev.darcosse.common.justenoughcobblemon.compat.jade.provider

import com.cobblemon.mod.common.block.PokeSnackBlock
import com.cobblemon.mod.common.block.entity.PokeSnackBlockEntity
import com.cobblemon.mod.common.client.tooltips.generateAdditionalBaitEffectTooltip
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeSupport
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeUids
import dev.darcosse.common.justenoughcobblemon.config.JecConfig
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.ui.IElementHelper

/**
 * Shows what a placed Poké Snack does: bites left, the bait effect rates of a lure snack and the
 * ingredients it was cooked with. Each line follows its mode in client.json.
 *
 * Client only: Cobblemon already sends the whole block entity to the client, so no server data
 * is needed and this works on servers without Jade.
 *
 * @author Darcosse
 * @version 1.1
 * @since 2026
 */
object PokeSnackProvider : IBlockComponentProvider {

    /**
     * A snack is removed when one more bite is taken at [PokeSnackBlock.MAX_BITES], so it can be
     * eaten MAX_BITES + 1 times in total. Changing this desynchronises the counter from the block.
     */
    private const val TOTAL_BITES = PokeSnackBlock.MAX_BITES + 1

    /**
     * Scale of the ingredient icons. 0.5 was too small to tell berries apart; 1.0 makes the line
     * as tall as two text lines. 0.75 keeps the count readable without stretching the tooltip.
     */
    private const val INGREDIENT_ICON_SCALE = 0.75F

    /**
     * Number of leading lines dropped from Cobblemon's bait effect tooltip. Cobblemon starts the
     * list with a blue "Bait Info:" header whenever there is at least one effect; the Jade
     * tooltip shows the rates alone. If Cobblemon removes that header, the first rate would be
     * dropped instead: set this to 0.
     */
    private const val BAIT_HEADER_LINES = 1

    /**
     * Last bait component turned into lines, compared by identity. Jade rebuilds the tooltip
     * every client tick while the snack is looked at, and building the lines creates an item stack
     * and merges every effect; the component only changes when the block is replaced.
     */
    private var cachedBaitSource: Any? = null
    private var cachedBaitLines: List<Component> = emptyList()

    /** Adds bites, bait rates and ingredients, each according to its display mode. */
    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val snack = accessor.blockEntity as? PokeSnackBlockEntity ?: return
        val modes = JecConfig.client().jade.pokeSnack

        if (JadeSupport.visible(modes.bitesLeft, accessor)) {
            val bites = accessor.blockState.getValue(PokeSnackBlock.BITES)
            tooltip.add(JadeSupport.text("poke_snack.bites", TOTAL_BITES - bites, TOTAL_BITES))
        }

        if (JadeSupport.visible(modes.baitEffects, accessor) && snack.isLure()) {
            tooltip.addAll(baitLines(snack))
        }

        if (JadeSupport.visible(modes.ingredients, accessor)) {
            appendIngredients(tooltip, snack.ingredientComponent?.ingredientIds.orEmpty())
        }
    }

    /**
     * Adds one row of ingredient icons. Identical ingredients are merged into a single icon with
     * a count, so a snack made of three identical berries does not take three slots.
     */
    private fun appendIngredients(tooltip: ITooltip, ingredientIds: List<ResourceLocation>) {
        val stacks = ingredientIds
            .groupingBy { it }
            .eachCount()
            .mapNotNull { (id, count) ->
                BuiltInRegistries.ITEM.getOptional(id).map { ItemStack(it, count) }.orElse(null)
            }
        if (stacks.isEmpty()) return

        val helper = IElementHelper.get()
        tooltip.add(JadeSupport.text("poke_snack.ingredients"))
        stacks.forEach { tooltip.append(helper.item(it, INGREDIENT_ICON_SCALE)) }
    }

    /**
     * Bait effect rates, produced by Cobblemon's own item tooltip helper so the wording and the
     * merging of duplicated effects stay identical to what the player sees on the item.
     */
    private fun baitLines(snack: PokeSnackBlockEntity): List<Component> {
        val source = snack.baitEffectsComponent ?: return emptyList()
        if (source !== cachedBaitSource) {
            cachedBaitLines = generateAdditionalBaitEffectTooltip(snack.toItemStack()).drop(BAIT_HEADER_LINES)
            cachedBaitSource = source
        }
        return cachedBaitLines
    }

    /** Always on in Jade: visibility is decided per line by client.json, not by Jade's settings. */
    override fun isRequired(): Boolean = true

    /** Provider id. */
    override fun getUid(): ResourceLocation = JadeUids.POKE_SNACK
}
