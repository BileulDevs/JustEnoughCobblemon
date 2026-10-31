package dev.darcosse.common.justenoughcobblemon.compat.jade.provider

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeSupport
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeUids
import dev.darcosse.common.justenoughcobblemon.config.DisplayMode
import dev.darcosse.common.justenoughcobblemon.config.JecConfig
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.theme.IThemeHelper
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Healing machine details: stored charge, time until fully charged, and time left on a heal in
 * progress.
 *
 * The charge itself comes from the client copy, which Cobblemon refreshes every tick while the
 * machine charges. The maximum charge, the time to full and the heal timer come from the server:
 * the maximum is not saved, and the charging speed is a server config value the client cannot
 * know. Without server data the bar falls back to the client's default maximum.
 *
 * @author Darcosse
 * @version 1.1
 * @since 2026
 */
object HealingMachineProvider : IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    /**
     * Payload format version. Increment whenever a key below is added, removed or changes type,
     * and keep [appendServerData] and [appendTooltip] in sync.
     */
    private const val PAYLOAD_VERSION = 1

    /** Maximum charge (Float). */
    private const val KEY_MAX_CHARGE = "m"

    /** Ticks until fully charged (Int). Absent when full, in use or charging is instant. */
    private const val KEY_TICKS_TO_FULL = "f"

    /** Ticks left on the heal in progress (Int). Absent when idle. */
    private const val KEY_HEAL_TICKS = "h"

    private const val TICKS_PER_SECOND = 20

    /** Adds either the infinite marker or the charge bar, then the timers. */
    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val machine = accessor.blockEntity as? HealingMachineBlockEntity ?: return
        val modes = JecConfig.client().jade.healingMachine
        val payload = JadeSupport.readPayload(accessor, JadeUids.HEALING_MACHINE, PAYLOAD_VERSION)
        val theme = IThemeHelper.get()

        if (JadeSupport.visible(modes.charge, accessor)) {
            if (machine.infinite) {
                tooltip.add(theme.success(JadeSupport.text("healing_machine.infinite")))
            } else {
                val maxCharge = payload?.takeIf { it.contains(KEY_MAX_CHARGE) }?.getFloat(KEY_MAX_CHARGE) ?: machine.maxCharge
                val ratio = if (maxCharge > 0F) machine.healingCharge / maxCharge else 0F
                val label = JadeSupport.text("healing_machine.charge", formatCharge(machine.healingCharge), formatCharge(maxCharge))
                tooltip.add(JadeSupport.bar(ratio, label, true))
            }
        }

        if (payload == null || !JadeSupport.visible(modes.timers, accessor)) return
        if (payload.contains(KEY_HEAL_TICKS)) {
            tooltip.add(JadeSupport.text("healing_machine.healing", theme.seconds(payload.getInt(KEY_HEAL_TICKS), accessor.tickRate())))
        } else if (payload.contains(KEY_TICKS_TO_FULL)) {
            tooltip.add(JadeSupport.text("healing_machine.full_in", theme.seconds(payload.getInt(KEY_TICKS_TO_FULL), accessor.tickRate())))
        }
    }

    /** One decimal is enough: a full party needs 6 charge, and the bar shows the rest. */
    private fun formatCharge(value: Float): String = String.format(Locale.ROOT, "%.1f", value)

    /**
     * Server side: the same arithmetic as Cobblemon's healing machine ticker, which adds
     * maxCharge / (secondsToChargeHealingMachine * 20) per tick while the machine is idle.
     */
    override fun appendServerData(data: CompoundTag, accessor: BlockAccessor) {
        if (!JecConfig.server().jade.healingMachine) return
        val machine = accessor.blockEntity as? HealingMachineBlockEntity ?: return
        val payload = CompoundTag()
        payload.putFloat(KEY_MAX_CHARGE, machine.maxCharge)

        if (machine.isInUse) {
            payload.putInt(KEY_HEAL_TICKS, machine.healTimeLeft)
        } else if (!machine.infinite && machine.healingCharge < machine.maxCharge && machine.maxCharge > 0F) {
            val secondsToFullCharge = Cobblemon.config.secondsToChargeHealingMachine.coerceAtLeast(0.0)
            if (secondsToFullCharge > 0.0) {
                val missingRatio = (machine.maxCharge - machine.healingCharge) / machine.maxCharge
                payload.putInt(KEY_TICKS_TO_FULL, (missingRatio * secondsToFullCharge * TICKS_PER_SECOND).roundToInt())
            }
        }

        JadeSupport.writePayload(data, JadeUids.HEALING_MACHINE, PAYLOAD_VERSION, payload)
    }

    /** Skips the request when the player set both lines to "never". */
    override fun shouldRequestData(accessor: BlockAccessor): Boolean {
        val modes = JecConfig.client().jade.healingMachine
        return modes.charge != DisplayMode.NEVER || modes.timers != DisplayMode.NEVER
    }

    /** Always on in Jade: visibility is decided per line by client.json, not by Jade's settings. */
    override fun isRequired(): Boolean = true

    /** Provider id. */
    override fun getUid(): ResourceLocation = JadeUids.HEALING_MACHINE
}
