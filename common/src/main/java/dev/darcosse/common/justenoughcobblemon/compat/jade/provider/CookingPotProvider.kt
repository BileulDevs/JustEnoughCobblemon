package dev.darcosse.common.justenoughcobblemon.compat.jade.provider

import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.block.campfirepot.CampfireBlock
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.cobblemon.mod.common.item.crafting.CookingPotRecipeBase
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeSupport
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeUids
import dev.darcosse.common.justenoughcobblemon.config.DisplayMode
import dev.darcosse.common.justenoughcobblemon.config.JecConfig
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.theme.IThemeHelper
import snownee.jade.api.ui.IElementHelper

/**
 * Cooking pot details: the dish the current ingredients make, with its seasoning applied, and
 * the cooking progress. Jade already lists the pot's contents when the server runs Jade, so the
 * ingredients themselves are not repeated.
 *
 * Server data only for the dish and the progress: recipes are resolved on the server, and the
 * client copy of the pot does not follow the cooking progress. Each line follows its mode in
 * client.json, and server.json can turn the server side off.
 *
 * @author Darcosse
 * @version 1.1
 * @since 2026
 */
object CookingPotProvider : IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    /**
     * Payload format version. Increment whenever a key below is added, removed or changes type,
     * and keep [appendServerData] and [appendTooltip] in sync.
     */
    private const val PAYLOAD_VERSION = 1

    /** Resulting dish, encoded with ItemStack.OPTIONAL_STREAM_CODEC through Jade's helper. */
    private const val KEY_RESULT = "r"

    /** Cooking progress and its target (Int), as exposed by the pot's own container data. */
    private const val KEY_PROGRESS = "p"
    private const val KEY_TOTAL = "t"

    /**
     * Adds the dish with a progress arrow, or a hint when the lid is open. The dish icon is
     * Jade's small inline item, sized to the text line: a full-size icon made the line taller
     * than the text and pushed the name off-centre.
     */
    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val payload = JadeSupport.readPayload(accessor, JadeUids.COOKING_POT, PAYLOAD_VERSION) ?: return
        val modes = JecConfig.client().jade.cookingPot
        val result = payload.get(KEY_RESULT)
            ?.let { accessor.decodeFromNbt(ItemStack.OPTIONAL_STREAM_CODEC, it).orElse(ItemStack.EMPTY) }
            ?: ItemStack.EMPTY
        if (result.isEmpty) return

        val helper = IElementHelper.get()
        val showDish = JadeSupport.visible(modes.dish, accessor)
        if (showDish) {
            tooltip.add(JadeSupport.text("cooking_pot.result"))
            tooltip.append(helper.smallItem(result))
            tooltip.append(helper.text(result.hoverName))
        }

        val total = payload.getInt(KEY_TOTAL)
        val progress = payload.getInt(KEY_PROGRESS)
        if (total > 0 && progress > 0 && JadeSupport.visible(modes.progress, accessor)) {
            val arrow = helper.progress(progress.toFloat() / total)
            // The arrow follows the dish when both are shown, and gets its own line otherwise.
            if (showDish) tooltip.append(arrow) else tooltip.add(arrow)
        }

        // In Cobblemon's server tick the pot only cooks while LID is true, i.e. the lid is on.
        if (!accessor.blockState.getValue(CampfireBlock.LID) && JadeSupport.visible(modes.lidWarning, accessor)) {
            tooltip.add(IThemeHelper.get().warning(JadeSupport.text("cooking_pot.lid_open")))
        }
    }

    /**
     * Server side: resolves the dish exactly like Cobblemon's CampfireBlockEntity.serverTick
     * (shaped recipes first, then shapeless, seasoning applied with the recipe's seasoning tag).
     *
     * This is a recipe lookup on each Jade data request while a player looks at the pot; it never
     * runs otherwise.
     */
    override fun appendServerData(data: CompoundTag, accessor: BlockAccessor) {
        if (!JecConfig.server().jade.cookingPot) return
        val pot = accessor.blockEntity as? CampfireBlockEntity ?: return
        val level = accessor.level

        val grid = CampfireBlockEntity.CRAFTING_GRID_SLOTS.map { pot.getItem(it) }
        val input = CraftingInput.of(CampfireBlockEntity.CRAFTING_GRID_WIDTH, CampfireBlockEntity.CRAFTING_GRID_WIDTH, grid)

        val recipe: CookingPotRecipeBase = level.recipeManager
            .getRecipeFor(CobblemonRecipeTypes.COOKING_POT_COOKING, input, level)
            .map<CookingPotRecipeBase> { it.value() }
            .or {
                level.recipeManager
                    .getRecipeFor(CobblemonRecipeTypes.COOKING_POT_SHAPELESS, input, level)
                    .map<CookingPotRecipeBase> { it.value() }
            }
            .orElse(null) ?: return

        val result = recipe.assemble(input, level.registryAccess())
        recipe.applySeasoning(result, pot.getSeasonings().filter { it.`is`(recipe.seasoningTag) })

        val payload = CompoundTag()
        payload.put(KEY_RESULT, accessor.encodeAsNbt(ItemStack.OPTIONAL_STREAM_CODEC, result))
        payload.putInt(KEY_PROGRESS, pot.dataAccess.get(CampfireBlockEntity.COOKING_PROGRESS_INDEX))
        payload.putInt(KEY_TOTAL, pot.dataAccess.get(CampfireBlockEntity.COOKING_PROGRESS_TOTAL_TIME_INDEX))

        JadeSupport.writePayload(data, JadeUids.COOKING_POT, PAYLOAD_VERSION, payload)
    }

    /** Skips the request (and its recipe lookup) when the player set every pot line to "never". */
    override fun shouldRequestData(accessor: BlockAccessor): Boolean {
        val modes = JecConfig.client().jade.cookingPot
        return listOf(modes.dish, modes.progress, modes.lidWarning).any { it != DisplayMode.NEVER }
    }

    /** Always on in Jade: visibility is decided per line by client.json, not by Jade's settings. */
    override fun isRequired(): Boolean = true

    /** Provider id. */
    override fun getUid(): ResourceLocation = JadeUids.COOKING_POT
}
