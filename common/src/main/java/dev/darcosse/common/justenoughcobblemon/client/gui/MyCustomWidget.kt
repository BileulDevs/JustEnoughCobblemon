package dev.darcosse.common.justenoughcobblemon.client.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.render.drawScaledText

class MyCustomWidget(x: Int, y: Int) : AbstractWidget(x, y, 86, 46, Component.empty()) {

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        // Titre au-dessus du widget, comme AbilitiesWidget
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Component.literal("Mon Titre").bold(),
            x = x + 9,
            y = y - 10,
            shadow = true
        )

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Component.literal("Mon Onglet custom").bold(),
            x,
            y,
            shadow = true
        )
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput) {}
}


