package dev.darcosse.common.justenoughcobblemon.network

import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

/**
 * Common network payload used by the client to request Pokémon spawn data from the server.
 * This class is platform-agnostic and works on both Fabric and NeoForge.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
class RequestSpawnDataPayload : CustomPacketPayload {

    /**
     * Companion object defining the unique packet identifier for network registration.
     */
    companion object {
        val TYPE = CustomPacketPayload.Type<RequestSpawnDataPayload>(
            ResourceLocation.fromNamespaceAndPath("justenoughcobblemon", "request_spawn_data")
        )
    }

    /**
     * Returns the unique type identifier for this packet payload.
     */
    override fun type(): CustomPacketPayload.Type<RequestSpawnDataPayload> = TYPE
}