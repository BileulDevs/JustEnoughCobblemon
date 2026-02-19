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
     * Updates the cache with a new set of spawn data and clears any existing entries.
     */
    fun populate(data: Map<String, List<SpawnInfo>>) {
        cache.clear()
        cache.putAll(data)
    }

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
    }
}