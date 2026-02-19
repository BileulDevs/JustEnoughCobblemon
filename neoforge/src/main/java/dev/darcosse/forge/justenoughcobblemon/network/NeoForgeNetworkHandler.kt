package dev.darcosse.forge.justenoughcobblemon.network

import dev.darcosse.common.justenoughcobblemon.network.RequestSpawnDataPayload
import dev.darcosse.common.justenoughcobblemon.network.SpawnDataCache
import dev.darcosse.common.justenoughcobblemon.network.SpawnDataPayload
import dev.darcosse.common.justenoughcobblemon.util.SpawnDataExtractor
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.StreamDecoder
import net.minecraft.network.codec.StreamEncoder
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar

/**
 * Handles the registration of network payloads and their respective handlers
 * for the NeoForge platform.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
object NeoForgeNetworkHandler {

    /**
     * Registers all custom network payloads (Client-to-Server and Server-to-Client)
     * and defines the codecs used for serialization and deserialization.
     */
    fun register(event: RegisterPayloadHandlersEvent) {
        val registrar: PayloadRegistrar = event.registrar("1")

        registrar.playToServer(
            RequestSpawnDataPayload.TYPE,
            StreamCodec.of(
                StreamEncoder { _, _ -> },
                StreamDecoder { _ -> RequestSpawnDataPayload() }
            )
        ) { payload, context ->
            context.enqueueWork {
                val allSpawns = SpawnDataExtractor.getAllSpawns()
                context.reply(SpawnDataPayload(allSpawns))
            }
        }

        registrar.playToClient(
            SpawnDataPayload.TYPE,
            StreamCodec.of(
                StreamEncoder { buf, payload -> SpawnDataPayload.encode(payload, buf as FriendlyByteBuf) },
                StreamDecoder { buf -> SpawnDataPayload.decode(buf as FriendlyByteBuf) }
            )
        ) { payload, context ->
            context.enqueueWork {
                SpawnDataCache.populate(payload.spawnData)
            }
        }
    }
}