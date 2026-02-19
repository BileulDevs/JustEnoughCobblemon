package dev.darcosse.common.justenoughcobblemon.network

import dev.darcosse.common.justenoughcobblemon.util.SpawnInfo
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

/**
 * Common network payload used to send the complete Pokémon spawn data map from
 * the server to the client. Handles complex serialization of SpawnInfo objects.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
class SpawnDataPayload(
    val spawnData: Map<String, List<SpawnInfo>>
) : CustomPacketPayload {

    /**
     * Companion object handling the networking ID and custom binary serialization
     * logic for spawn data.
     */
    companion object {
        val TYPE = CustomPacketPayload.Type<SpawnDataPayload>(
            ResourceLocation.fromNamespaceAndPath("justenoughcobblemon", "spawn_data")
        )

        /**
         * Decodes the binary buffer into a structured map of spawn information.
         */
        fun decode(buf: FriendlyByteBuf): SpawnDataPayload {
            val size = buf.readInt()
            val map = HashMap<String, List<SpawnInfo>>(size)
            repeat(size) {
                val speciesId = buf.readUtf()
                val spawns = buf.readList { decodeSpawnInfo(it as FriendlyByteBuf) }
                map[speciesId] = spawns
            }
            return SpawnDataPayload(map)
        }

        /**
         * Encodes the spawn data map into a binary buffer for network transmission.
         */
        fun encode(payload: SpawnDataPayload, buf: FriendlyByteBuf) {
            buf.writeInt(payload.spawnData.size)
            for ((speciesId, spawns) in payload.spawnData) {
                buf.writeUtf(speciesId)
                buf.writeCollection(spawns) { b, spawn ->
                    encodeSpawnInfo(spawn, b as FriendlyByteBuf)
                }
            }
        }

        /**
         * Serializes an individual SpawnInfo object.
         */
        private fun encodeSpawnInfo(info: SpawnInfo, buf: FriendlyByteBuf) {
            buf.writeUtf(info.bucket)
            buf.writeFloat(info.weight)
            buf.writeNullable(info.levelRange) { b, r -> b.writeInt(r.first); b.writeInt(r.last) }
            buf.writeNullable(info.form) { b, v -> b.writeUtf(v) }
            buf.writeCollection(info.biomes) { b, v -> b.writeUtf(v) }
            buf.writeCollection(info.anticonditionBiomes) { b, v -> b.writeUtf(v) }
            buf.writeCollection(info.structures) { b, v -> b.writeUtf(v) }
            buf.writeCollection(info.dimensions) { b, v -> b.writeUtf(v) }
            buf.writeNullable(info.minY) { b, v -> b.writeFloat(v) }
            buf.writeNullable(info.maxY) { b, v -> b.writeFloat(v) }
            buf.writeNullable(info.minLight) { b, v -> b.writeInt(v) }
            buf.writeNullable(info.maxLight) { b, v -> b.writeInt(v) }
            buf.writeNullable(info.minSkyLight) { b, v -> b.writeInt(v) }
            buf.writeNullable(info.maxSkyLight) { b, v -> b.writeInt(v) }
            buf.writeNullable(info.timeRange) { b, v -> b.writeUtf(v) }
            buf.writeNullable(info.moonPhase) { b, v -> b.writeUtf(v) }
            buf.writeNullable(info.canSeeSky) { b, v -> b.writeBoolean(v) }
            buf.writeNullable(info.isRaining) { b, v -> b.writeBoolean(v) }
            buf.writeNullable(info.isThundering) { b, v -> b.writeBoolean(v) }
            buf.writeNullable(info.isSlimeChunk) { b, v -> b.writeBoolean(v) }
            buf.writeCollection(info.markers) { b, v -> b.writeUtf(v) }
            buf.writeCollection(info.labels) { b, v -> b.writeUtf(v) }
        }

        /**
         * Deserializes an individual SpawnInfo object from the buffer.
         */
        private fun decodeSpawnInfo(buf: FriendlyByteBuf): SpawnInfo = SpawnInfo(
            bucket = buf.readUtf(),
            weight = buf.readFloat(),
            levelRange = buf.readNullable { b -> b.readInt()..b.readInt() },
            form = buf.readNullable { it.readUtf() },
            biomes = buf.readList { it.readUtf() },
            anticonditionBiomes = buf.readList { it.readUtf() },
            structures = buf.readList { it.readUtf() },
            dimensions = buf.readList { it.readUtf() },
            minY = buf.readNullable { it.readFloat() },
            maxY = buf.readNullable { it.readFloat() },
            minLight = buf.readNullable { it.readInt() },
            maxLight = buf.readNullable { it.readInt() },
            minSkyLight = buf.readNullable { it.readInt() },
            maxSkyLight = buf.readNullable { it.readInt() },
            timeRange = buf.readNullable { it.readUtf() },
            moonPhase = buf.readNullable { it.readUtf() },
            canSeeSky = buf.readNullable { it.readBoolean() },
            isRaining = buf.readNullable { it.readBoolean() },
            isThundering = buf.readNullable { it.readBoolean() },
            isSlimeChunk = buf.readNullable { it.readBoolean() },
            markers = buf.readList { it.readUtf() },
            labels = buf.readList { it.readUtf() }
        )
    }

    /**
     * Returns the packet type identifier.
     */
    override fun type(): CustomPacketPayload.Type<SpawnDataPayload> = TYPE
}