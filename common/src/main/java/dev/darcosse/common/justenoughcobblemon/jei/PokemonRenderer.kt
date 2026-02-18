package dev.darcosse.common.justenoughcobblemon.jei

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Utility object used to render 3D Pokémon models within JEI interfaces.
 * * @author Darcosse
 * @version 1.0
 * @since 2026
 */
object PokemonRenderer {
    private val poseState = FloatingState()
    private val rotationQuaternion = Quaternionf()
    private val rotationEuler = Vector3f(10f, 35f, 0f)

    /**
     * Renders a specific Pokémon species at the given coordinates.
     * @param graphics The current GuiGraphics instance.
     * @param speciesName The internal name of the Pokémon species.
     * @param x X coordinate for rendering.
     * @param y Y coordinate for rendering.
     */
    fun render(graphics: GuiGraphics, speciesName: String, x: Int, y: Int) {
        val species = PokemonSpecies.getByName(speciesName.lowercase()) ?: return
        val pokemon = RenderablePokemon(species, emptySet())

        val poseStack = graphics.pose()
        poseStack.pushPose()

        val modelHeight = species.baseScale
        val dynamicOffsetY = y.toFloat() + (modelHeight * 0.5f)

        poseStack.translate(x.toFloat(), dynamicOffsetY, 200f)

        val renderTick = Minecraft.getInstance().level?.gameTime ?: 0L
        val animationTicks = (renderTick % 1000).toFloat() / 10000f

        drawProfilePokemon(
            renderablePokemon = pokemon,
            matrixStack = poseStack,
            rotation = rotationQuaternion.identity().fromEulerXYZDegrees(rotationEuler),
            state = poseState,
            partialTicks = animationTicks,
            scale = 25f
        )

        poseStack.popPose()
    }
}