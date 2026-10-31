package dev.darcosse.fabric.justenoughcobblemon.compat;

import dev.darcosse.common.justenoughcobblemon.compat.jade.JustEnoughCobblemonJadePlugin;

/**
 * Fabric entry point for Jade. It only exists to be named in the "jade" entrypoint of
 * fabric.mod.json, which Jade reads on Fabric; every registration lives in
 * {@link JustEnoughCobblemonJadePlugin} so both loaders stay identical.
 *
 * <p>Fabric only instantiates an entrypoint when a mod asks for it, so without Jade this class is
 * never loaded and the Jade API it depends on is never needed.</p>
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
public class JustEnoughCobblemonFabricJadePlugin extends JustEnoughCobblemonJadePlugin {

    /**
     * Instantiated reflectively by Fabric Loader, which requires a public no-argument constructor.
     */
    public JustEnoughCobblemonFabricJadePlugin() {}
}
