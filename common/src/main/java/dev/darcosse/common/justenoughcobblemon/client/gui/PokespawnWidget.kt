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

class PokespawnWidget(x: Int, y: Int) : InfoTextScrollWidget(pX = x, pY = y) {

    companion object {
        private val arrowLeft = cobblemonResource("textures/gui/pokedex/info_arrow_left.png")
        private val arrowRight = cobblemonResource("textures/gui/pokedex/info_arrow_right.png")
    }

    val leftButton: ScaledButton = ScaledButton(
        x + 2.5F,
        y - 8F,
        7,
        10,
        arrowLeft,
        clickAction = { switchEntry(false) }
    )

    val rightButton: ScaledButton = ScaledButton(
        x + 133F,
        y - 8F,
        7,
        10,
        arrowRight,
        clickAction = { switchEntry(true) }
    )

    var spawns: List<SpawnInfo> = emptyList()
        private set

    var selectedIndex: Int = 0

    fun setSpawns(list: List<SpawnInfo>) {
        spawns = list
        selectedIndex = 0
        refreshText()
    }

    private fun switchEntry(next: Boolean) {
        if (spawns.isEmpty()) return
        selectedIndex = if (next) {
            (selectedIndex + 1) % spawns.size
        } else {
            (selectedIndex - 1 + spawns.size) % spawns.size
        }
        refreshText()
    }

    private fun refreshText() {
        if (spawns.isEmpty()) {
            setText(listOf(Component.translatable("justenoughcobblemon.ui.spawn.no_data").string))
        } else {
            setText(spawns[selectedIndex].toDisplayLines())
        }
        scrollAmount = 0.0
    }

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