package dev.darcosse.common.justenoughcobblemon.compat.jade.provider

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeSupport
import dev.darcosse.common.justenoughcobblemon.compat.jade.JadeUids
import dev.darcosse.common.justenoughcobblemon.config.JecConfig
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.EntityAccessor
import snownee.jade.api.IEntityComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

/**
 * Shows the EVs a Pokémon gives when defeated, e.g. "EV yield: 2 Attack, 1 Speed".
 *
 * Client only: the yield is species data, which Cobblemon syncs to every client, read through
 * the entity's current form so regional and other forms show their own yield. Shown always by
 * default; client.json can restrict it to sneaking or hide it.
 *
 * @author Darcosse
 * @version 1.2
 * @since 2026
 */
object EvYieldProvider : IEntityComponentProvider {

    /** Separator between stats, kept outside the translations as it is the same everywhere. */
    private const val SEPARATOR = ", "

    /** Adds one line listing every stat with a non-zero yield, in the usual stat order. */
    override fun appendTooltip(tooltip: ITooltip, accessor: EntityAccessor, config: IPluginConfig) {
        if (!JadeSupport.visible(JecConfig.client().jade.pokemon.evYield, accessor)) return
        // Explicit smart cast instead of "(x as? T)?.y ?: return": that chain makes the null path
        // of T and of y share one bytecode label, and recomputing frames there loads T. On the
        // NeoForge dev runtime the Architectury transformer does exactly that against unpatched
        // vanilla classes, and loading a Cobblemon entity or block entity there crashes.
        val entity = accessor.entity
        if (entity !is PokemonEntity) return
        val pokemon = entity.pokemon
        val yields = pokemon.form.evYield

        val parts = Stats.PERMANENT.mapNotNull { stat ->
            val amount = yields[stat] ?: 0
            if (amount > 0) JadeSupport.text("ev_yield.entry", amount, stat.displayName) else null
        }
        if (parts.isEmpty()) return

        val joined = Component.empty()
        parts.forEachIndexed { index, part ->
            if (index > 0) joined.append(SEPARATOR)
            joined.append(part)
        }
        tooltip.add(JadeSupport.text("ev_yield", joined))
    }

    /** Always on in Jade: visibility is decided per line by client.json, not by Jade's settings. */
    override fun isRequired(): Boolean = true

    /** Provider id. */
    override fun getUid(): ResourceLocation = JadeUids.EV_YIELD
}
