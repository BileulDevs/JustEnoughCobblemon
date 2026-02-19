package dev.darcosse.forge.justenoughcobblemon;

import dev.darcosse.forge.justenoughcobblemon.network.NeoForgeNetworkHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Main mod entry point for the NeoForge platform.
 * Handles event bus registration and network handler initialization.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
@Mod("justenoughcobblemon")
public class JustEnoughCobblemon {

    /**
     * Initializes the mod, registers the instance to the NeoForge event bus,
     * and sets up the network communication system.
     */
    public JustEnoughCobblemon(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(NeoForgeNetworkHandler.INSTANCE::register);
    }

    /**
     * Handles the registration of custom commands when the server starts.
     */
    @SubscribeEvent
    public void onCommandRegistration(RegisterCommandsEvent event) {

    }
}