package dev.darcosse.fabric.justenoughcobblemon;

import dev.darcosse.common.justenoughcobblemon.config.JecConfig;
import dev.darcosse.fabric.justenoughcobblemon.network.FabricNetworkHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main mod entry point for the Fabric platform.
 * Responsible for initializing server-side logic and logger setup.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
public class JustEnoughCobblemon implements ModInitializer {

    /**
     * Static logger instance for the mod.
     */
    public static Logger LOGGER = LogManager.getLogger("JustEnoughCobblemon");

    /**
     * Initializes the mod by loading its config files, setting up the server-side network
     * handlers and logging the startup sequence.
     */
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing...");

        // First, so that anything initialised afterwards already reads the player's settings.
        FabricLoader loader = FabricLoader.getInstance();
        JecConfig.init(loader.getConfigDir(), loader.getEnvironmentType() == EnvType.CLIENT);

        FabricNetworkHandler.INSTANCE.registerServer();
    }

}