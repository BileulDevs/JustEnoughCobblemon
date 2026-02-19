package dev.darcosse.forge.justenoughcobblemon.client.event;

import dev.darcosse.common.justenoughcobblemon.network.RequestSpawnDataPayload;
import dev.darcosse.common.justenoughcobblemon.network.SpawnDataCache;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client-side event subscriber for NeoForge to handle network lifecycle
 * and data synchronization for Pokémon spawn information.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
@EventBusSubscriber(modid = "justenoughcobblemon", value = Dist.CLIENT)
public class NeoForgeClientEvents {

    /**
     * Triggered when the client player logs into a server.
     * Sends a request to the server to synchronize spawn data.
     */
    @SubscribeEvent
    public static void onClientConnected(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) {
        PacketDistributor.sendToServer(new RequestSpawnDataPayload());
    }

    /**
     * Triggered when the client player logs out.
     * Clears the local spawn data cache to ensure data integrity for the next session.
     */
    @SubscribeEvent
    public static void onClientDisconnected(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        SpawnDataCache.INSTANCE.clear();
    }
}