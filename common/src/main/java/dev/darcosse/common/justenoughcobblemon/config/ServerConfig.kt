package dev.darcosse.common.justenoughcobblemon.config

import com.google.gson.annotations.SerializedName

/**
 * Server settings, stored in `config/justenoughcobblemon/server.json`.
 *
 * Only what the server decides lives here: which data it agrees to send to players. On a
 * dedicated server this is the server's file; in single player the integrated server reads the
 * player's own copy. Fields are `var` with an initializer for the same reason as in
 * [ClientConfig].
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
class ServerConfig {

    /** Reminder written into the file, since JSON has no comments. Not read by the mod. */
    @SerializedName("_help")
    var help: String = "Controls which data this server sends to players' Jade tooltips. Turning one off hides the server-dependent lines of that tooltip for everyone. Changes apply within a second, no restart needed."

    /** Format version of this file, for future migrations. Not meant to be edited. */
    var configVersion: Int = 1

    /** Server data sent to Jade tooltips. */
    var jade: Jade = Jade()

    /**
     * One switch per tooltip that needs server data, plus the limits that bound that data.
     */
    class Jade {
        /** Time until the next growth stage, mulch uses left. */
        var berryTree = true

        /** Owner, count and list of pastured Pokémon. Turn off to keep pastures private. */
        var pasture = true

        /**
         * Pokémon listed by name on a pasture. Each one is a PC lookup per tooltip refresh, and the
         * list grows the tooltip; the rest is summarised as "... and N more". Clamped to
         * [MAX_PASTURE_LISTED].
         */
        var pastureMaxListed = 6

        /** Maximum charge, time to full charge, heal timer. */
        var healingMachine = true

        /** Fossil, resulting Pokémon, organic matter, restoration progress. */
        var fossilMachine = true

        /** Resulting dish and cooking progress (one recipe lookup per tooltip refresh). */
        var cookingPot = true

        /** [pastureMaxListed] within the accepted range. */
        fun pastureMaxListedClamped(): Int = pastureMaxListed.coerceIn(0, MAX_PASTURE_LISTED)
    }

    companion object {
        /**
         * Upper bound of [Jade.pastureMaxListed]: beyond this the tooltip no longer fits on most
         * screens. Raising it only costs payload size and PC lookups.
         */
        const val MAX_PASTURE_LISTED = 32
    }
}
