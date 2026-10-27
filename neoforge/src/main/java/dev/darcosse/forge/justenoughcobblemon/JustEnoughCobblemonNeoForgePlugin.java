package dev.darcosse.forge.justenoughcobblemon;

import dev.darcosse.common.justenoughcobblemon.JustEnoughCobblemonPluginCommon;
import mezz.jei.api.JeiPlugin;

/**
 * NeoForge entry point for JEI. It only exists to carry {@link JeiPlugin}, which JEI discovers per loader;
 * every registration lives in {@link JustEnoughCobblemonPluginCommon} so both loaders stay identical.
 *
 * @author Darcosse
 * @version 1.1
 * @since 2026
 */
@JeiPlugin
public class JustEnoughCobblemonNeoForgePlugin extends JustEnoughCobblemonPluginCommon {

    /**
     * Instantiated reflectively by JEI, which requires a public no-argument constructor.
     */
    public JustEnoughCobblemonNeoForgePlugin() {}
}
