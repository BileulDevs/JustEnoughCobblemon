package dev.darcosse.common.justenoughcobblemon.network

import dev.darcosse.common.justenoughcobblemon.util.SpawnInfo
import net.minecraft.resources.ResourceLocation

/**
 * Client-side cache that stores Pokémon spawn information received from the server.
 * This cache is used to provide data to the UI without direct access to the
 * server-side spawn pools.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
object SpawnDataCache {
    private val cache = mutableMapOf<String, List<SpawnInfo>>()

    /**
     * Whether the connected server has the mod.
     *
     * Needed to tell two very different situations apart, which otherwise both
     * show up as an empty cache: a server that answered and simply has no spawn
     * for this species, and a server that has no idea this mod exists. The
     * first is information, the second is a setup problem, and the player
     * deserves to know which one they are looking at.
     */
    private var serverSupported = true

    /**
     * Updates the cache with a new set of spawn data and clears any existing entries.
     */
    fun populate(data: Map<String, List<SpawnInfo>>) {
        cache.clear()
        cache.putAll(data)
        serverSupported = true
    }

    /** Set on join, from whether the server declared our channel. */
    fun markServerSupported(supported: Boolean) {
        serverSupported = supported
    }

    /** False when the connected server does not have the mod installed. */
    fun isServerSupported(): Boolean = serverSupported

    /**
     * Retrieves the list of spawn information for a specific species from the local cache.
     */
    fun getSpawnsForSpecies(speciesId: ResourceLocation): List<SpawnInfo> {
        return cache[speciesId.path] ?: emptyList()
    }

    /**
     * Clears all stored data from the cache, typically called upon disconnection.
     */
    fun clear() {
        cache.clear()
        // Assume support until a join tells us otherwise: single player never
        // goes through the join check, and there the integrated server always
        // has the mod.
        serverSupported = true
    }
}