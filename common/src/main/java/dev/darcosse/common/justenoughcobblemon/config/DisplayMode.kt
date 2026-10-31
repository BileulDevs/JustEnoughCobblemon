package dev.darcosse.common.justenoughcobblemon.config

import com.google.gson.annotations.SerializedName

/**
 * When a tooltip line is shown. Every line of every Jade tooltip has its own mode in client.json.
 *
 * The JSON names are lowercase so the file reads naturally; an unknown value falls back to the
 * line's default when the file is loaded (see [ConfigFile]).
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
enum class DisplayMode {

    /** Always shown. */
    @SerializedName("always")
    ALWAYS,

    /**
     * Shown only while Jade's "show details" input is held, which is Shift (sneak) by default,
     * plus Jade's optional dedicated key if the player bound one.
     */
    @SerializedName("sneak")
    SNEAK,

    /** Never shown. */
    @SerializedName("never")
    NEVER;

    /**
     * Whether a line in this mode is visible, given whether the details input is held.
     */
    fun isVisible(detailsHeld: Boolean): Boolean = when (this) {
        ALWAYS -> true
        SNEAK -> detailsHeld
        NEVER -> false
    }
}
