package dev.darcosse.common.justenoughcobblemon.client.gui.pokedex.widgets

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

class SpawnInfoWidget(x: Int, y: Int, width: Int, height: Int) :
    AbstractWidget(x, y, width, height, Component.literal("Spawn Info")) {

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        // C'est ici qu'on dessine le contenu de TA page
        graphics.drawString(
            Minecraft.getInstance().font,
            "§lLieux d'apparition",
            x + 5, y + 5,
            0xFFFFFF
        )

        graphics.drawString(
            Minecraft.getInstance().font,
            "§7Recherche des biomes...",
            x + 5, y + 20,
            0xAAAAAA
        )
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {}
}