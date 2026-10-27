package dev.darcosse.common.justenoughcobblemon.jei

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Draws animated Cobblemon 3D models inside JEI pages.
 *
 * Each species and form keeps its own animation state: Cobblemon's states remember the current model,
 * aspects, pose and elapsed time, so sharing one state between the several recipes JEI shows at once made
 * every model fight over it each frame.
 *
 * @author Darcosse
 * @version 1.4
 * @since 2026
 */
object PokemonRenderer {

    /** Camera angles in degrees (pitch, yaw, roll); a three-quarter view that shows most silhouettes. */
    private val VIEW_ANGLES_DEGREES = Vector3f(10f, 35f, 0f)

    /**
     * Base size handed to Cobblemon, which then applies the model's own profile scale and the
     * species' base scale. Raising it enlarges every model and pushes big ones out of the model area.
     */
    private const val MODEL_SCALE = 25f

    /**
     * Depth of the model's centre. Vanilla draws tooltips at z = 400 and the category background at z = 0,
     * so the model's half-depth must fit on both sides: closer to 400 and big models cover the tooltips,
     * closer to 0 and their back half disappears behind the background.
     */
    private const val MODEL_Z_OFFSET = 200f

    /** Fraction of the species' base scale used to push tall models down so they stay centred. */
    private const val BASE_SCALE_Y_FACTOR = 0.5f

    /** Length of one game tick; Cobblemon animations are expressed in ticks. */
    private const val NANOS_PER_TICK = 50_000_000f

    /**
     * Upper bound on the time fed to the animation in one frame. Without it, reopening JEI after a
     * few minutes would fast-forward the animation by thousands of ticks in a single step.
     */
    private const val MAX_FRAME_DELTA_TICKS = 2f

    /**
     * Identifies one displayed appearance. Aspects are part of the key because Alolan and Kantonian Rattata
     * share a species id but need different models, and each model needs its own animation state.
     */
    private data class ModelKey(val speciesId: ResourceLocation, val aspects: Set<String>)

    /**
     * Per-appearance rendering data. The renderable is cached because it is immutable for our use and was
     * previously allocated every frame for every visible recipe.
     */
    private class ModelEntry(val renderable: RenderablePokemon) {
        val state = FloatingState().apply {
            // drawProfilePokemon sets the aspects itself, but only after our depth compensation has looked the
            // model up; without this, the first frame of a form would use the base form's profile scale.
            currentAspects = renderable.aspects
        }
        var lastFrameNanos = System.nanoTime()
    }

    /**
     * Bounded by the number of species and forms ever displayed (a few thousand at most), each entry being
     * a few small objects; no eviction needed. Only touched from the render thread.
     */
    private val entries = HashMap<ModelKey, ModelEntry>()

    private val rotation = Quaternionf()

    /**
     * Draws a species in the form selected by [aspects], advancing its own animation by the real time elapsed
     * since it was last drawn. Called every frame for every visible recipe (not every tick).
     *
     * @param speciesId full identifier, so species outside the `cobblemon` namespace resolve too
     * @param aspects   form aspects ("alolan", "heat-appliance", ...); empty for the base form
     */
    fun render(graphics: GuiGraphics, speciesId: ResourceLocation, aspects: Set<String>, x: Int, y: Int) {
        val entry = entryFor(ModelKey(speciesId, aspects)) ?: return

        val poseStack = graphics.pose()
        poseStack.pushPose()

        // The form's base scale, not the species': forms such as Alolan Exeggutor differ greatly in size.
        val offsetY = y.toFloat() + entry.renderable.form.baseScale * BASE_SCALE_Y_FACTOR
        poseStack.translate(x.toFloat(), offsetY, MODEL_Z_OFFSET)
        poseStack.scale(1f, 1f, depthCompensation(speciesId, entry))

        drawProfilePokemon(
            renderablePokemon = entry.renderable,
            matrixStack = poseStack,
            // Rebuilt every call rather than cached: the quaternion is passed by reference into Cobblemon's renderer.
            rotation = rotation.identity().fromEulerXYZDegrees(VIEW_ANGLES_DEGREES),
            state = entry.state,
            // Cobblemon adds this value to the state's clock (FloatingState.updatePartialTicks accumulates),
            // so it must be the time since this state's previous frame, not an absolute time.
            partialTicks = consumeFrameDelta(entry),
            scale = MODEL_SCALE
        )

        poseStack.popPose()
    }

    /**
     * Returns the depth factor that gives the model the same proportions in depth as on screen.
     *
     * Cobblemon's `drawProfilePokemon` applies `scale(p, p, 1 / p)` with `p` the model's profile scale
     * (read from the 1.8.4 bytecode). Big Pokémon have a small `p`, so while they shrink on screen
     * their depth grows by `1 / p`, ending up `1 / p²` times deeper than wide: Wailord, Wailmer or Aggron
     * then reach past z = 400 and cover the tooltips. Multiplying depth by `p²` first cancels that out.
     * It also restores uniform normals, so big models are lit like small ones.
     */
    private fun depthCompensation(speciesId: ResourceLocation, entry: ModelEntry): Float {
        // Same lookup Cobblemon performs inside drawProfilePokemon, so it resolves the same model variant.
        // Safe call kept although the poser is probably non-null: its nullability could not be read from the
        // compiled jar, and a warning is cheaper than a compile error.
        val profileScale = VaryingModelRepository.getPoser(speciesId, entry.state)?.profileScale ?: return 1f
        // A zero or invalid scale would flatten the model to nothing; leaving depth untouched is the safe fallback.
        if (!profileScale.isFinite() || profileScale <= 0f) return 1f
        return profileScale * profileScale
    }

    /**
     * Returns the cached entry of an appearance, creating it on first display, or `null` when the species
     * no longer exists (datapack removed between two JEI reloads).
     */
    private fun entryFor(key: ModelKey): ModelEntry? {
        val species = PokemonSpecies.getByIdentifier(key.speciesId) ?: return null
        val cached = entries[key]
        // Species data is re-synced when joining a server or after /reload; if Cobblemon hands out a new
        // instance, a renderable built on the old one would keep showing the previous definition.
        if (cached != null && cached.renderable.species === species) return cached
        return ModelEntry(RenderablePokemon(species, key.aspects)).also { entries[key] = it }
    }

    /**
     * Measures the real time elapsed since this entry was last drawn, in ticks.
     * Measured per entry so that two pages of the same species visible together do not advance
     * the shared animation twice per frame, and so the animation runs in menus where no level ticks.
     */
    private fun consumeFrameDelta(entry: ModelEntry): Float {
        val now = System.nanoTime()
        val deltaTicks = (now - entry.lastFrameNanos) / NANOS_PER_TICK
        entry.lastFrameNanos = now
        return deltaTicks.coerceIn(0f, MAX_FRAME_DELTA_TICKS)
    }
}
