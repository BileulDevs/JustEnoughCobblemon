package dev.darcosse.common.justenoughcobblemon.compat.jade

import net.minecraft.resources.ResourceLocation

/**
 * Every identifier this mod registers with Jade.
 *
 * Each id names one provider and keys its payload in the server data. Settings no longer go
 * through Jade: every provider is marked required (`isRequired() = true`) so Jade shows no
 * switch for it, and all choices live in config/justenoughcobblemon/client.json and server.json.
 *
 * @author Darcosse
 * @version 1.1
 * @since 2026
 */
object JadeUids {

    private const val NAMESPACE = "justenoughcobblemon"

    val POKE_SNACK: ResourceLocation = id("poke_snack")
    val DISC_SHELF: ResourceLocation = id("disc_shelf")
    val BERRY_TREE: ResourceLocation = id("berry_tree")
    val PASTURE: ResourceLocation = id("pasture")
    val HEALING_MACHINE: ResourceLocation = id("healing_machine")
    val FOSSIL_MACHINE: ResourceLocation = id("fossil_machine")
    val COOKING_POT: ResourceLocation = id("cooking_pot")
    val PLANTS: ResourceLocation = id("plants")
    val DISPLAY_CASE: ResourceLocation = id("display_case")
    val EV_YIELD: ResourceLocation = id("ev_yield")

    /** Builds an id in this mod's namespace. */
    private fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(NAMESPACE, path)
}
