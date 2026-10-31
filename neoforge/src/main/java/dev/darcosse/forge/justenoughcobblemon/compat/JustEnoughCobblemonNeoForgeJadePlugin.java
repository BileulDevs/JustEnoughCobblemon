package dev.darcosse.forge.justenoughcobblemon.compat;

import dev.darcosse.common.justenoughcobblemon.compat.jade.JustEnoughCobblemonJadePlugin;
import snownee.jade.api.WailaPlugin;

/**
 * NeoForge entry point for Jade. It only exists to carry {@link WailaPlugin}, which Jade looks up
 * in the mod scan data on NeoForge; every registration lives in
 * {@link JustEnoughCobblemonJadePlugin} so both loaders stay identical.
 *
 * <p>Only Jade reads that annotation, so without Jade this class is never loaded. The annotation
 * value is left empty on purpose: it names a mod required in addition to Jade, and none is.</p>
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
@WailaPlugin
public class JustEnoughCobblemonNeoForgeJadePlugin extends JustEnoughCobblemonJadePlugin {

    /**
     * Instantiated reflectively by Jade, which requires a public no-argument constructor.
     */
    public JustEnoughCobblemonNeoForgeJadePlugin() {}
}
