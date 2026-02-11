package dev.darcosse.common.justenoughcobblemon.client.gui.pokedex.pages

import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.BASE_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.BASE_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.ScaledButton
import dev.darcosse.common.justenoughcobblemon.client.gui.pokedex.widgets.SpawnInfoWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class SpawnPokedexPage(val parent: PokedexGUI) {

    fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val xOffset = (parent.width - BASE_WIDTH) / 2
        val yOffset = (parent.height - BASE_HEIGHT) / 2

        val x = xOffset + 185
        val y = yOffset + 140

        graphics.drawString(
            Minecraft.getInstance().font,
            Component.literal("Lieux d'apparition"),
            x, y, 0xFFFFFF
        )
    }

    companion object {
        @JvmStatic
        fun createTabButton(parent: PokedexGUI, x: Float, y: Float): ScaledButton {
            val icon = ResourceLocation("cobblemon", "textures/gui/pokedex/tab_drops.png")

            return ScaledButton(x, y, 16, 16, icon) {
                // 1. On vide l'affichage actuel
                parent.displaytabInfoElement(-1, true)

                // 2. On calcule la zone d'affichage (le rectangle de droite)
                val xPos = ((parent.width - 340) / 2) + 180
                val yPos = ((parent.height - 204) / 2) + 26

                // 3. On crée et on ajoute notre widget directement au Pokedex
                val spawnWidget = SpawnInfoWidget(xPos, yPos, 145, 150)

                // On utilise l'Accessor pour l'ajouter aux renderables du Screen
                (parent as? dev.darcosse.common.justenoughcobblemon.mixin.ScreenAccessor)
                    ?.callAddRenderableWidget(spawnWidget)

                println("Widget de spawn injecté !")
            }
        }
    }
}