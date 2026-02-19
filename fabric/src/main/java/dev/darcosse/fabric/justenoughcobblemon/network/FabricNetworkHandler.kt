package dev.darcosse.fabric.justenoughcobblemon.network

import dev.darcosse.common.justenoughcobblemon.network.RequestSpawnDataPayload
import dev.darcosse.common.justenoughcobblemon.network.SpawnDataCache
import dev.darcosse.common.justenoughcobblemon.network.SpawnDataPayload
import dev.darcosse.common.justenoughcobblemon.util.SpawnDataExtractor
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.StreamDecoder
import net.minecraft.network.codec.StreamEncoder

/**
 * Handles the registration of network payloads and their respective handlers
 * for the Fabric platform, covering both client and server sides.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
object FabricNetworkHandler {

    /**
     * Registers server-side packet types and defines the global receiver
     * for processing spawn data requests from clients.
     */
    fun registerServer() {
        PayloadTypeRegistry.playC2S().register(
            RequestSpawnDataPayload.TYPE,
            StreamCodec.of(
                StreamEncoder { _, _ -> },
                StreamDecoder { _ -> RequestSpawnDataPayload() }
            )
        )

        PayloadTypeRegistry.playS2C().register(
            SpawnDataPayload.TYPE,
            StreamCodec.of(
                StreamEncoder { buf, payload -> SpawnDataPayload.encode(payload, buf as FriendlyByteBuf) },
                StreamDecoder { buf -> SpawnDataPayload.decode(buf as FriendlyByteBuf) }
            )
        )

        ServerPlayNetworking.registerGlobalReceiver(RequestSpawnDataPayload.TYPE) { payload, context ->
            context.server().execute {
                val allSpawns = SpawnDataExtractor.getAllSpawns()
                context.responseSender().sendPacket(SpawnDataPayload(allSpawns))
            }
        }
    }

    /**
     * Registers client-side event listeners and receivers to manage
     * the synchronization and caching of spawn data.
     */
    fun registerClient() {
        ClientPlayConnectionEvents.JOIN.register { _, sender, _ ->
            sender.sendPacket(RequestSpawnDataPayload())
        }

        ClientPlayNetworking.registerGlobalReceiver(SpawnDataPayload.TYPE) { payload, context ->
            context.client().execute {
                SpawnDataCache.populate(payload.spawnData)
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            SpawnDataCache.clear()
        }
    }
}