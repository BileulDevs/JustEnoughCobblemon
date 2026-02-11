package dev.darcosse.forge.justenoughcobblemon;

import dev.darcosse.common.justenoughcobblemon.JustEnoughCobblemonPluginCommon;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;

@JeiPlugin
public class JustEnoughCobblemonNeoForgePlugin extends JustEnoughCobblemonPluginCommon {
    public JustEnoughCobblemonNeoForgePlugin() {
        super();
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        super.registerCategories(registration);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        super.registerRecipes(registration);
    }
}