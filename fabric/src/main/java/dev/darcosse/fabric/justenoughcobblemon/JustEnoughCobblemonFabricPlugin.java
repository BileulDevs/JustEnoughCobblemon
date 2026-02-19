package dev.darcosse.fabric.justenoughcobblemon;

import dev.darcosse.common.justenoughcobblemon.JustEnoughCobblemonPluginCommon;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;

/**
 * Fabric-specific implementation of the JEI plugin.
 * Extends the common plugin logic to register Pokémon-related recipes and categories.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
@JeiPlugin
public class JustEnoughCobblemonFabricPlugin extends JustEnoughCobblemonPluginCommon {

    /**
     * Default constructor for the JEI plugin.
     */
    public JustEnoughCobblemonFabricPlugin() {}

    /**
     * Registers recipes into JEI by calling the shared logic defined in the common module.
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        super.registerRecipes(registration);
    }
}