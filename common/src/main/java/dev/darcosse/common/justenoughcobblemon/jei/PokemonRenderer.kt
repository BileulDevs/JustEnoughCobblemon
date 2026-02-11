package dev.darcosse.common.justenoughcobblemon.jei

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.gui.GuiGraphics
import org.joml.Quaternionf
import org.joml.Vector3f

object PokemonRenderer {
    private val poseState = FloatingState()

    fun render(graphics: GuiGraphics, speciesName: String, x: Int, y: Int) {
        val species = PokemonSpecies.getByName(speciesName.lowercase()) ?: return
        val pokemon = RenderablePokemon(species, emptySet())

        val poseStack = graphics.pose()
        poseStack.pushPose()

        val modelHeight = species.baseScale
        val dynamicOffsetY = y.toFloat() + (modelHeight * 0.5f)

        poseStack.translate(x.toFloat(), dynamicOffsetY, 200f)

        val dynamicScale = 22f / (modelHeight.coerceAtLeast(1.0f) * 0.5f)

        drawProfilePokemon(
            renderablePokemon = pokemon,
            matrixStack = poseStack,
            rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(10f, 35f, 0f)),
            state = poseState,
            partialTicks = (System.currentTimeMillis() % 1000).toFloat() / 5000f,
            scale = 25f
        )

        poseStack.popPose()
    }
}