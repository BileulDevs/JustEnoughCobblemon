package dev.darcosse.common.justenoughcobblemon.client.gui

import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.pokedex.ScaledButton
import com.cobblemon.mod.common.client.gui.pokedex.widgets.InfoTextScrollWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import dev.darcosse.common.justenoughcobblemon.util.SpawnInfo
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

/**
 * Custom widget for displaying Pokémon spawn information in the Pokedex GUI.
 * Extends InfoTextScrollWidget to support multi-line scrolling and pagination.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
class PokespawnWidget(x: Int, y: Int) : InfoTextScrollWidget(pX = x, pY = y) {

    /**
     * Companion object holding the textures for the navigation arrows.
     */
    companion object {
        private val arrowLeft = cobblemonResource("textures/gui/pokedex/info_arrow_left.png")
        private val arrowRight = cobblemonResource("textures/gui/pokedex/info_arrow_right.png")
    }

    /**
     * Button to navigate to the previous spawn entry.
     */
    val leftButton: ScaledButton = ScaledButton(
        x + 2.5F,
        y - 8F,
        7,
        10,
        arrowLeft,
        clickAction = { switchEntry(false) }
    )

    /**
     * Button to navigate to the next spawn entry.
     */
    val rightButton: ScaledButton = ScaledButton(
        x + 133F,
        y - 8F,
        7,
        10,
        arrowRight,
        clickAction = { switchEntry(true) }
    )

    /**
     * The list of spawn information data currently loaded.
     */
    var spawns: List<SpawnInfo> = emptyList()
        private set

    /**
     * The index of the currently displayed spawn entry.
     */
    var selectedIndex: Int = 0

    /**
     * Updates the spawn list and resets the selection to the first entry.
     */
    fun setSpawns(list: List<SpawnInfo>) {
        spawns = list
        selectedIndex = 0
        refreshText()
    }

    /**
     * Switches between different spawn entries based on navigation input.
     */
    private fun switchEntry(next: Boolean) {
        if (spawns.isEmpty()) return
        selectedIndex = if (next) {
            (selectedIndex + 1) % spawns.size
        } else {
            (selectedIndex - 1 + spawns.size) % spawns.size
        }
        refreshText()
    }

    /**
     * Updates the text content of the scroll widget and resets the scroll position.
     */
    private fun refreshText() {
        if (spawns.isEmpty()) {
            setText(listOf("No spawn data."))
        } else {
            setText(spawns[selectedIndex].toDisplayLines())
        }
        scrollAmount = 0.0
    }

    /**
     * Renders the widget, including the pagination title and the scrolled content.
     */
    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val title = if (spawns.isEmpty()) {
            "No spawn data."
        } else {
            "Spawn ${selectedIndex + 1}/${spawns.size}"
        }

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Component.literal(title).bold(),
            x = pX + 9,
            y = pY - 10,
            shadow = true
        )

        super.renderWidget(context, mouseX, mouseY, delta)
    }
}