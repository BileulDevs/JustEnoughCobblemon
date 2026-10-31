package dev.darcosse.common.justenoughcobblemon.compat.jade.provider

import com.cobblemon.mod.common.api.mulch.MulchVariant
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.block.entity.BerryBlockEntity
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeSupport
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeUids
import dev.darcosse.common.justenoughcobblemon.config.DisplayMode
import dev.darcosse.common.justenoughcobblemon.config.JecConfig
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.theme.IThemeHelper

/**
 * Berry tree details: growth stage, mulch, and time until the next stage.
 *
 * Jade already shows the growth percentage of berry trees natively (its crop progress line
 * recognises the age property), so this provider adds the stage name instead of a second bar.
 *
 * The stage and the mulch type come from the block state (client only). The timer and the mulch
 * uses left need the server: the client copy of the tree is only refreshed when the block
 * changes, so its timer is stale.
 *
 * @author Darcosse
 * @version 1.1
 * @since 2026
 */
object BerryTreeProvider : IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    /**
     * Payload format version. Increment whenever a key below is added, removed or changes type,
     * and keep [appendServerData] and [appendTooltip] in sync.
     * 2: harvest preview removed.
     */
    private const val PAYLOAD_VERSION = 2

    /** Estimated ticks until the next growth stage (Int). Absent once the tree bears fruit. */
    private const val KEY_STAGE_TICKS = "t"

    /** Uses left on the current mulch (Int). */
    private const val KEY_MULCH_USES = "m"

    /** Namespace and suffix of Cobblemon's mulch items, used to display a mulch by its item name. */
    private const val MULCH_ITEM_NAMESPACE = "cobblemon"
    private const val MULCH_ITEM_SUFFIX = "_mulch"

    /** Adds stage, mulch and timer, each according to its display mode. */
    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val state = accessor.blockState
        val modes = JecConfig.client().jade.berryTree
        val payload = JadeSupport.readPayload(accessor, JadeUids.BERRY_TREE, PAYLOAD_VERSION)

        if (JadeSupport.visible(modes.stage, accessor)) {
            val age = state.getValue(BerryBlock.AGE)
            tooltip.add(JadeSupport.text("berry_tree.stage", JadeSupport.text("berry_tree.stage.$age")))
        }

        val mulch = state.getValue(BerryBlock.MULCH)
        if (mulch != MulchVariant.NONE && JadeSupport.visible(modes.mulch, accessor)) {
            val mulchName = mulchName(mulch)
            // Only limited mulches have a meaningful use counter; the others last until removed.
            if (mulch.duration > 0 && payload != null && payload.contains(KEY_MULCH_USES)) {
                tooltip.add(JadeSupport.text("berry_tree.mulch_uses", mulchName, payload.getInt(KEY_MULCH_USES)))
            } else {
                tooltip.add(JadeSupport.text("berry_tree.mulch", mulchName))
            }
        }

        if (state.getValue(BerryBlock.IS_ROOTED)) return

        if (JadeSupport.visible(modes.nextStage, accessor) && payload != null && payload.contains(KEY_STAGE_TICKS)) {
            val seconds = IThemeHelper.get().seconds(payload.getInt(KEY_STAGE_TICKS), accessor.tickRate())
            tooltip.add(JadeSupport.text("berry_tree.next_stage", seconds))
        }
    }

    /** Display name of a mulch, taken from its item so it follows Cobblemon's translations. */
    private fun mulchName(mulch: MulchVariant): Component {
        val itemId = ResourceLocation.fromNamespaceAndPath(MULCH_ITEM_NAMESPACE, mulch.serializedName + MULCH_ITEM_SUFFIX)
        return BuiltInRegistries.ITEM.getOptional(itemId)
            .map<Component> { it.description }
            .orElse(Component.literal(mulch.serializedName))
    }

    /**
     * Server side: estimated time to the next stage and mulch uses left. Runs on each Jade data
     * request while a player looks at the tree, unless server.json turns it off.
     */
    override fun appendServerData(data: CompoundTag, accessor: BlockAccessor) {
        if (!JecConfig.server().jade.berryTree) return
        val tree = accessor.blockEntity as? BerryBlockEntity ?: return
        val payload = CompoundTag()

        val age = accessor.blockState.getValue(BerryBlock.AGE)
        if (age < BerryBlock.FRUIT_AGE) {
            // The timer is only decremented on random ticks, in one batch covering the time since
            // the previous one; subtract that pending time to show what is really left.
            val pending = if (tree.lastTickTime > 0L) accessor.level.gameTime - tree.lastTickTime else 0L
            payload.putInt(KEY_STAGE_TICKS, (tree.stageTimer - pending).coerceAtLeast(0L).toInt())
        }

        payload.putInt(KEY_MULCH_USES, tree.mulchDuration)

        JadeSupport.writePayload(data, JadeUids.BERRY_TREE, PAYLOAD_VERSION, payload)
    }

    /**
     * Skips the request when every server-dependent line is set to "never": the stage needs no
     * server data, so only the mulch and the timer count.
     */
    override fun shouldRequestData(accessor: BlockAccessor): Boolean {
        val modes = JecConfig.client().jade.berryTree
        return modes.nextStage != DisplayMode.NEVER || modes.mulch != DisplayMode.NEVER
    }

    /** Always on in Jade: visibility is decided per line by client.json, not by Jade's settings. */
    override fun isRequired(): Boolean = true

    /** Provider id. */
    override fun getUid(): ResourceLocation = JadeUids.BERRY_TREE
}
