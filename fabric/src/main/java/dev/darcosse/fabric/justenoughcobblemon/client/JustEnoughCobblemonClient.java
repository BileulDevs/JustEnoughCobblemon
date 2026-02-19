package dev.darcosse.fabric.justenoughcobblemon.client;

import dev.darcosse.common.justenoughcobblemon.network.RequestSpawnDataPayload;
import dev.darcosse.common.justenoughcobblemon.network.SpawnDataCache;
import dev.darcosse.common.justenoughcobblemon.network.SpawnDataPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client-side entry point for the Fabric platform.
 * Manages the registration of network receivers and lifecycle events
 * to synchronize Pokémon spawn data with the server.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
public class JustEnoughCobblemonClient implements ClientModInitializer {

    /**
     * Initializes the client-side logic, including packet reception,
     * automatic data requests on server join, and cache management.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SpawnDataPayload.Companion.getTYPE(), (payload, context) ->
                context.client().execute(() ->
                        SpawnDataCache.INSTANCE.populate(payload.getSpawnData())
                )
        );

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                sender.sendPacket(new RequestSpawnDataPayload())
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                SpawnDataCache.INSTANCE.clear()
        );
    }
}