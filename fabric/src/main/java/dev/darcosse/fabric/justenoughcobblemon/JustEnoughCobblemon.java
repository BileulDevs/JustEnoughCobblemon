package dev.darcosse.fabric.justenoughcobblemon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JustEnoughCobblemon implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger("JustEnoughCobblemon");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing...");
    }

}
