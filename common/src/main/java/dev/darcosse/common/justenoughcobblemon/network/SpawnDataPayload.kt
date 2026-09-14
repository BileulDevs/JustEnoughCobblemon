package dev.darcosse.common.justenoughcobblemon.network

import dev.darcosse.common.justenoughcobblemon.util.MultiplierInfo
import dev.darcosse.common.justenoughcobblemon.util.SpawnInfo
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

/**
 * Common network payload used to send the complete Pokémon spawn data map from
 * the server to the client. Handles complex serialization of SpawnInfo objects.
 *
 * @author Darcosse
 * @version 1.1
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
         * Wire format version.
         *
         * The SpawnInfo codec below is positional: fields are written and read in
         * a fixed order with no names and no lengths. Add, remove or reorder one
         * field and a client running a different build will keep reading — it
         * will simply interpret the wrong bytes as the wrong types, and show
         * nonsense without ever raising an error.
         *
         * Bumping this on every change to encodeSpawnInfo / decodeSpawnInfo turns
         * that silent corruption into a clean, diagnosable refusal.
         */
        const val WIRE_VERSION = 6

        /**
         * Decodes the binary buffer into a structured map of spawn information.
         *
         * @throws IllegalStateException when the sender speaks a different wire
         *         version. Refusing here is the point: reading on would produce
         *         plausible-looking garbage.
         */
        fun decode(buf: FriendlyByteBuf): SpawnDataPayload {
            val version = buf.readVarInt()

            check(version == WIRE_VERSION) {
                "Just Enough Cobblemon: spawn data wire version mismatch " +
                        "(got $version, expected $WIRE_VERSION). " +
                        "The server and the client are running different versions of the mod."
            }

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
            buf.writeVarInt(WIRE_VERSION)

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
         *
         * MUST stay in the same order as decodeSpawnInfo, and any change here
         * MUST come with a WIRE_VERSION bump.
         */
        private fun encodeSpawnInfo(info: SpawnInfo, buf: FriendlyByteBuf) {
            buf.writeUtf(info.bucket)
            buf.writeNullable(info.spawnablePosition) { b, v -> b.writeUtf(v) }
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
            buf.writeNullable(info.isPokeSnack) { b, v -> b.writeBoolean(v) }
            buf.writeNullable(info.minX) { b, v -> b.writeFloat(v) }
            buf.writeNullable(info.maxX) { b, v -> b.writeFloat(v) }
            buf.writeNullable(info.minZ) { b, v -> b.writeFloat(v) }
            buf.writeNullable(info.maxZ) { b, v -> b.writeFloat(v) }
            buf.writeCollection(info.markers) { b, v -> b.writeUtf(v) }
            buf.writeCollection(info.labels) { b, v -> b.writeUtf(v) }
            buf.writeCollection(info.multipliers) { b, m -> encodeMultiplier(m, b as FriendlyByteBuf) }
            buf.writeCollection(info.neededBaseBlocks) { b, v -> b.writeUtf(v) }
            buf.writeCollection(info.neededNearbyBlocks) { b, v -> b.writeUtf(v) }
        }

        private fun encodeMultiplier(info: MultiplierInfo, buf: FriendlyByteBuf) {
            buf.writeFloat(info.multiplier)
            buf.writeCollection(info.conditions) { b, v -> b.writeUtf(v) }
            buf.writeCollection(info.anticonditions) { b, v -> b.writeUtf(v) }
            buf.writeBoolean(info.hasAnyCondition)
        }

        private fun decodeMultiplier(buf: FriendlyByteBuf): MultiplierInfo = MultiplierInfo(
            multiplier = buf.readFloat(),
            conditions = buf.readList { it.readUtf() },
            anticonditions = buf.readList { it.readUtf() },
            hasAnyCondition = buf.readBoolean()
        )

        /**
         * Deserializes an individual SpawnInfo object from the buffer.
         *
         * MUST stay in the same order as encodeSpawnInfo.
         */
        private fun decodeSpawnInfo(buf: FriendlyByteBuf): SpawnInfo = SpawnInfo(
            bucket = buf.readUtf(),
            spawnablePosition = buf.readNullable { it.readUtf() },
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
            isPokeSnack = buf.readNullable { it.readBoolean() },
            minX = buf.readNullable { it.readFloat() },
            maxX = buf.readNullable { it.readFloat() },
            minZ = buf.readNullable { it.readFloat() },
            maxZ = buf.readNullable { it.readFloat() },
            markers = buf.readList { it.readUtf() },
            labels = buf.readList { it.readUtf() },
            multipliers = buf.readList { decodeMultiplier(it as FriendlyByteBuf) },
            neededBaseBlocks = buf.readList { it.readUtf() },
            neededNearbyBlocks = buf.readList { it.readUtf() }
        )
    }

    /**
     * Returns the packet type identifier.
     */
    override fun type(): CustomPacketPayload.Type<SpawnDataPayload> = TYPE
}
