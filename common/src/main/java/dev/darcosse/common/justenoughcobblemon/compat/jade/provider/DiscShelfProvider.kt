package dev.darcosse.common.justenoughcobblemon.compat.jade.provider

import com.cobblemon.mod.common.block.DiscShelfBlock
import com.cobblemon.mod.common.block.entity.DiscShelfBlockEntity
import com.cobblemon.mod.common.item.components.TMMoveComponent
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeSupport
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeUids
import dev.darcosse.common.justenoughcobblemon.config.JecConfig
import net.minecraft.core.Direction
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
 * Shows the disc the player is pointing at on a Disc Shelf (with the move details when it is a
 * TM), the number of stored discs when no disc is pointed at, and every disc stored. Each line
 * follows its mode in client.json.
 *
 * Client only: the shelf sends its whole inventory to the client on every change.
 *
 * @author Darcosse
 * @version 1.2
 * @since 2026
 */
object DiscShelfProvider : IBlockComponentProvider {

    /*
     * Slot geometry, copied from the private DiscShelfBlockEntity.getHitSlot in Cobblemon 1.8.4.
     * If Cobblemon changes the shelf layout, the highlighted disc will no longer match the one
     * the player clicks; these values must then be updated from the new getHitSlot.
     */

    /** Number of slots; the shelf is a 2 x 7 grid. */
    private const val SLOT_COUNT = 14

    /** Number of rows, counted from the top in slot indices. */
    private const val ROW_COUNT = 7

    /** One texture pixel in block units. */
    private const val PIXEL = 1.0 / 16.0

    private const val LEFT_COLUMN_MIN_X = 1 * PIXEL
    private const val LEFT_COLUMN_MAX_X = 7 * PIXEL
    private const val RIGHT_COLUMN_MIN_X = 9 * PIXEL
    private const val RIGHT_COLUMN_MAX_X = 15 * PIXEL
    private const val SLOTS_MIN_Y = 1 * PIXEL
    private const val SLOTS_MAX_Y = 15 * PIXEL
    private const val ROW_HEIGHT = 2 * PIXEL

    /** Value returned by [hitSlot] when the player is not pointing at a slot. */
    private const val NO_SLOT = -1

    /** Scale of the content icons; 0.5 fits two rows of discs in the height of one line. */
    private const val CONTENT_ICON_SCALE = 0.5F

    /** Placeholder for a stat a move does not have (status moves, never-miss moves). */
    private const val MISSING_STAT = "—"

    /** Replaces Jade's block icon with the pointed disc, the same way Jade does for bookshelves. */
    override fun getIcon(accessor: BlockAccessor, config: IPluginConfig, currentIcon: IElement?): IElement? {
        if (!JadeSupport.visible(JecConfig.client().jade.discShelf.pointedDisc, accessor)) return null
        val pointed = pointedStack(accessor)
        return if (pointed.isEmpty) null else IElementHelper.get().item(pointed)
    }

    /**
     * Adds the pointed disc first, then the shelf contents. The disc count describes the shelf as
     * a whole, so it is left out while a disc is pointed at: the tooltip is then about that disc.
     */
    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val shelf = accessor.blockEntity as? DiscShelfBlockEntity ?: return
        val modes = JecConfig.client().jade.discShelf

        val pointed = pointedStack(accessor)
        if (!pointed.isEmpty) {
            if (JadeSupport.visible(modes.pointedDisc, accessor)) {
                tooltip.add(JadeSupport.itemName(pointed))
            }
            if (JadeSupport.visible(modes.moveDetails, accessor)) {
                appendMoveDetails(tooltip, pointed)
            }
        } else if (JadeSupport.visible(modes.discCount, accessor)) {
            tooltip.add(JadeSupport.text("disc_shelf.count", shelf.items.count { !it.isEmpty }, SLOT_COUNT))
        }

        if (JadeSupport.visible(modes.contents, accessor)) {
            appendContents(tooltip, shelf)
        }
    }

    /**
     * Type, category, power, accuracy and PP of the move a TM teaches. Does nothing for other
     * discs (music discs, blank TMs, Upgrade, Dubious Disc).
     */
    private fun appendMoveDetails(tooltip: ITooltip, stack: ItemStack) {
        val move = TMMoveComponent.getTMMove(stack) ?: return

        tooltip.add(JadeSupport.text("disc_shelf.move_type", move.elementalType.displayName, move.damageCategory.displayName))

        // Cobblemon stores "no value" as zero or a negative number (status moves have no power,
        // never-miss moves have no accuracy), which would read as "0" or "-1%".
        val power: Any = if (move.power > 0) move.power.toInt() else MISSING_STAT
        val accuracy: Any = if (move.accuracy > 0) "${move.accuracy.toInt()}%" else MISSING_STAT
        tooltip.add(JadeSupport.text("disc_shelf.move_stats", power, accuracy, move.pp))
    }

    /**
     * One row of icons of the stored discs. Jade's own item grid is removed when this row is
     * shown: it only appears when the server also runs Jade, and would duplicate this row.
     */
    private fun appendContents(tooltip: ITooltip, shelf: DiscShelfBlockEntity) {
        val stored = shelf.items.filter { !it.isEmpty }
        tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE)
        if (stored.isEmpty()) return

        val helper = IElementHelper.get()
        tooltip.add(helper.item(stored.first(), CONTENT_ICON_SCALE))
        stored.drop(1).forEach { tooltip.append(helper.item(it, CONTENT_ICON_SCALE)) }
    }

    /** Stack in the slot the player points at, or an empty stack. */
    private fun pointedStack(accessor: BlockAccessor): ItemStack {
        val shelf = accessor.blockEntity as? DiscShelfBlockEntity ?: return ItemStack.EMPTY
        val slot = hitSlot(accessor)
        return if (slot == NO_SLOT) ItemStack.EMPTY else shelf.getItem(slot)
    }

    /**
     * Slot under the crosshair, mirroring Cobblemon's private DiscShelfBlockEntity.getHitSlot.
     * Only the front face has slots; any other face, the frame or the gap between the two columns
     * returns [NO_SLOT].
     */
    private fun hitSlot(accessor: BlockAccessor): Int {
        val hit = accessor.hitResult
        val facing = accessor.blockState.getValue(DiscShelfBlock.FACING)
        if (hit.direction != facing) return NO_SLOT

        val pos = hit.blockPos
        val relX = hit.location.x - pos.x
        val relY = hit.location.y - pos.y
        val relZ = hit.location.z - pos.z

        // Face-local X goes from the viewer's left (0) to right (1), whatever way the shelf faces.
        val faceX = when (facing) {
            Direction.NORTH -> 1.0 - relX
            Direction.SOUTH -> relX
            Direction.WEST -> relZ
            Direction.EAST -> 1.0 - relZ
            else -> return NO_SLOT
        }

        val column = when (faceX) {
            in LEFT_COLUMN_MIN_X..LEFT_COLUMN_MAX_X -> 0
            in RIGHT_COLUMN_MIN_X..RIGHT_COLUMN_MAX_X -> 1
            else -> return NO_SLOT
        }
        if (relY < SLOTS_MIN_Y || relY > SLOTS_MAX_Y) return NO_SLOT

        val rowFromBottom = ((relY - SLOTS_MIN_Y) / ROW_HEIGHT).toInt().coerceIn(0, ROW_COUNT - 1)
        val row = ROW_COUNT - 1 - rowFromBottom
        val slot = row * 2 + column
        return if (slot in 0 until SLOT_COUNT) slot else NO_SLOT
    }

    /**
     * Runs after Jade's item storage line so that [appendContents] can remove it; see
     * [JadeSupport.ITEM_STORAGE_PRIORITY].
     */
    override fun getDefaultPriority(): Int = JadeSupport.ITEM_STORAGE_PRIORITY + 1

    /** Always on in Jade: visibility is decided per line by client.json, not by Jade's settings. */
    override fun isRequired(): Boolean = true

    /** Provider id. */
    override fun getUid(): ResourceLocation = JadeUids.DISC_SHELF
}
