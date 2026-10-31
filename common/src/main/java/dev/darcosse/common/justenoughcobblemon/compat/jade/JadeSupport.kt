package dev.darcosse.common.justenoughcobblemon.compat.jade

import com.cobblemon.mod.common.item.components.TMMoveComponent
import dev.darcosse.common.justenoughcobblemon.config.DisplayMode
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import snownee.jade.api.Accessor
import snownee.jade.api.TooltipPosition
import snownee.jade.api.ui.BoxStyle
import snownee.jade.api.ui.IElement
import snownee.jade.api.ui.IElementHelper

/**
 * Small helpers shared by every Jade provider of this mod: translated text, display modes,
 * progress bars, and the versioned payload exchanged with the server.
 *
 * Nothing here may touch client-only Minecraft classes: providers that also send server data are
 * loaded on dedicated servers, and they call into this object.
 *
 * @author Darcosse
 * @version 1.3
 * @since 2026
 */
object JadeSupport {

    /** Prefix of every tooltip string this integration displays. */
    private const val TEXT_PREFIX = "justenoughcobblemon.jade."

    /**
     * Key of the format version inside each payload. Kept to one character because the payload
     * travels every time Jade refreshes the tooltip.
     */
    private const val VERSION_KEY = "v"

    /**
     * Fill colour of progress bars: the teal of Cobblemon's PC screen (#34AEB0), opaque. Jade
     * darkens it automatically for the bar's lower half.
     */
    private const val BAR_COLOR: Int = 0xFF34AEB0.toInt()

    /**
     * Text colour on progress bars. Jade switches to black text on light bars by default, which
     * is unreadable on the teal above; forcing white keeps it legible at any fill level.
     */
    private const val BAR_TEXT_COLOR: Int = 0xFFFFFFFF.toInt()

    /**
     * Priority of Jade's built-in item storage line ("jade:item_storage") for blocks. It is not
     * part of Jade's API, so it is copied here from `ItemStorageProvider.getDefaultPriority()` in
     * Jade 15.10.x. Providers that remove that line must run after it; if Jade moves it later, the
     * removal silently stops working and the item grid shows up next to our own list.
     */
    const val ITEM_STORAGE_PRIORITY: Int = TooltipPosition.BODY + 1000

    /**
     * Display name of an item shown in a tooltip line.
     *
     * Every TM shares the item name "Technical Machine": the taught move only lives in the item's
     * data component, and Cobblemon adds it as a separate line of the item tooltip, which Jade
     * does not show. A TM is therefore named after its move alone ("Will-O-Wisp"); the icon
     * already shows it is a TM. Other items, blank TMs included, keep their item name.
     */
    fun itemName(stack: ItemStack): Component {
        val move = TMMoveComponent.getTMMove(stack) ?: return stack.hoverName
        return move.displayName
    }

    /** Translatable component for one of this integration's strings. */
    fun text(key: String, vararg args: Any): MutableComponent = Component.translatable(TEXT_PREFIX + key, *args)

    /**
     * Whether a line in [mode] is visible right now. "Sneak" follows Jade's show-details input,
     * which is Shift by default, so this mod and Jade's own detailed view react to the same key.
     */
    fun visible(mode: DisplayMode, accessor: Accessor<*>): Boolean = mode.isVisible(accessor.showDetails())

    /**
     * Progress bar in the PC teal with white text, used for every bar of this mod.
     *
     * [label] should be plain text: styled parts (Jade's coloured durations, for instance) keep
     * their own colour and ignore the white.
     */
    fun bar(ratio: Float, label: Component, canDecrease: Boolean): IElement {
        val helper = IElementHelper.get()
        val style = helper.progressStyle().color(BAR_COLOR).textColor(BAR_TEXT_COLOR)
        return helper.progress(ratio.coerceIn(0F, 1F), label, style, BoxStyle.getNestedBox(), canDecrease)
    }

    /**
     * Copy of [styled] as unstyled text, for labels drawn on a bar. Resolves translations, so only
     * call it on the client.
     */
    fun plain(styled: Component): Component = Component.literal(styled.string)

    /**
     * Stores a provider's payload under its own id, stamped with the format version.
     *
     * Each provider owns its sub-tag, so two providers can never overwrite each other's keys in
     * the shared tag Jade sends back.
     */
    fun writePayload(data: CompoundTag, uid: ResourceLocation, version: Int, payload: CompoundTag) {
        payload.putInt(VERSION_KEY, version)
        data.put(uid.toString(), payload)
    }

    /**
     * Reads a provider's payload back on the client.
     *
     * Returns null when the server sent nothing (no Jade or no mod on the server, data turned off
     * in server.json) or when the server runs a different format version: reading a payload
     * written by another version would display wrong values rather than fail loudly.
     */
    fun readPayload(accessor: Accessor<*>, uid: ResourceLocation, version: Int): CompoundTag? {
        val data = accessor.serverData
        val key = uid.toString()
        if (!data.contains(key, Tag.TAG_COMPOUND.toInt())) return null
        val payload = data.getCompound(key)
        return if (payload.getInt(VERSION_KEY) == version) payload else null
    }
}
