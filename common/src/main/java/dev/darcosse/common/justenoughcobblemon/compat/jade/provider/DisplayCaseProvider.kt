package dev.darcosse.common.justenoughcobblemon.compat.jade.provider

import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeSupport
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeUids
import dev.darcosse.common.justenoughcobblemon.config.JecConfig
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.JadeIds
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.ui.IElement
import snownee.jade.api.ui.IElementHelper

/**
 * Shows the item exhibited in a Display Case: its icon replaces the block icon and its name is
 * added below the block name.
 *
 * Client only: the case sends its item to the client on every change.
 *
 * @author Darcosse
 * @version 1.3
 * @since 2026
 */
object DisplayCaseProvider : IBlockComponentProvider {

    /** Uses the displayed item as the tooltip icon. */
    override fun getIcon(accessor: BlockAccessor, config: IPluginConfig, currentIcon: IElement?): IElement? {
        if (!JadeSupport.visible(JecConfig.client().jade.displayCase.item, accessor)) return null
        val stack = displayedStack(accessor)
        return if (stack.isEmpty) null else IElementHelper.get().item(stack)
    }

    /**
     * Adds the item name. Jade's item grid, shown when the server runs Jade, is removed because
     * it would repeat the same single item.
     */
    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        if (!JadeSupport.visible(JecConfig.client().jade.displayCase.item, accessor)) return
        val stack = displayedStack(accessor)
        if (stack.isEmpty) return
        tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE)
        tooltip.add(JadeSupport.itemName(stack))
    }

    /** Item in the case, or an empty stack. */
    private fun displayedStack(accessor: BlockAccessor): ItemStack {
        // Explicit smart cast instead of "(x as? T)?.y ?: return": that chain makes the null path
        // of T and of y share one bytecode label, and recomputing frames there loads T. On the
        // NeoForge dev runtime the Architectury transformer does exactly that against unpatched
        // vanilla classes, and loading a Cobblemon entity or block entity there crashes.
        val case = accessor.blockEntity
        if (case !is DisplayCaseBlockEntity) return ItemStack.EMPTY
        return case.getStack()
    }

    /** Runs after Jade's item storage line so it can be removed; see [JadeSupport.ITEM_STORAGE_PRIORITY]. */
    override fun getDefaultPriority(): Int = JadeSupport.ITEM_STORAGE_PRIORITY + 1

    /** Always on in Jade: visibility is decided per line by client.json, not by Jade's settings. */
    override fun isRequired(): Boolean = true

    /** Provider id. */
    override fun getUid(): ResourceLocation = JadeUids.DISPLAY_CASE
}
