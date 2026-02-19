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
 * Custom widget for displaying Pokémon spawn information within the Pokedex GUI.
 * Inherits from InfoTextScrollWidget to provide a scrollable text area with pagination.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
class PokespawnWidget(x: Int, y: Int) : InfoTextScrollWidget(pX = x, pY = y) {

    /**
     * Static resources for the navigation UI.
     */
    companion object {
        private val arrowLeft = cobblemonResource("textures/gui/pokedex/info_arrow_left.png")
        private val arrowRight = cobblemonResource("textures/gui/pokedex/info_arrow_right.png")
    }

    /**
     * Button used to navigate to the previous spawn entry.
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
     * Button used to navigate to the next spawn entry.
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
     * The list of available spawn information for the current Pokémon.
     */
    var spawns: List<SpawnInfo> = emptyList()
        private set

    /**
     * Index of the currently displayed spawn entry.
     */
    var selectedIndex: Int = 0

    /**
     * Sets the spawn data list and resets the display to the first entry.
     */
    fun setSpawns(list: List<SpawnInfo>) {
        spawns = list
        selectedIndex = 0
        refreshText()
    }

    /**
     * Cycles through spawn entries based on navigation input.
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
     * Updates the scrollable text area with the content of the currently selected spawn.
     */
    private fun refreshText() {
        if (spawns.isEmpty()) {
            setText(listOf(Component.translatable("justenoughcobblemon.ui.spawn.no_data").string))
        } else {
            setText(spawns[selectedIndex].toDisplayLines())
        }
        scrollAmount = 0.0
    }

    /**
     * Renders the widget title, pagination information, and the scrollable content.
     */
    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val title = if (spawns.isEmpty()) {
            Component.translatable("justenoughcobblemon.ui.spawn.no_data")
        } else {
            Component.translatable(
                "justenoughcobblemon.ui.spawn.title",
                selectedIndex + 1,
                spawns.size
            )
        }

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = title.bold(),
            x = pX + 9,
            y = pY - 10,
            shadow = true
        )

        super.renderWidget(context, mouseX, mouseY, delta)
    }
}