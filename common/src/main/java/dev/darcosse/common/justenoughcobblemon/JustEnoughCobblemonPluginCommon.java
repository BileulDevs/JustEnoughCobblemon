package dev.darcosse.common.justenoughcobblemon;

import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Species;
import dev.darcosse.common.justenoughcobblemon.jei.PokemonDropCategory;
import dev.darcosse.common.justenoughcobblemon.recipes.PokemonDropRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JeiPlugin
public abstract class JustEnoughCobblemonPluginCommon implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("justenoughcobblemon", "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PokemonDropCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        System.out.println("[JustEnoughCobblemon] Enregistrement des recettes...");

        List<PokemonDropRecipe> recipes = new ArrayList<>();

        for (Species species : PokemonSpecies.getSpecies()) {
            List<ItemStack> drops = new ArrayList<>();

            Map<ItemStack, Float> dropRates = new HashMap<>();

            species.getDrops().getEntries().forEach(dropData -> {
                if (dropData instanceof com.cobblemon.mod.common.api.drop.ItemDropEntry itemDrop) {
                    ResourceLocation itemRes = itemDrop.getItem();
                    if (itemRes != null) {
                        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemRes);
                        ItemStack stack = new ItemStack(item, itemDrop.getQuantity());

                        drops.add(stack);
                        dropRates.put(stack, itemDrop.getPercentage());
                    }
                }
            });

            if (!drops.isEmpty()) {
                recipes.add(new PokemonDropRecipe(
                        species.getTranslatedName().getString(),
                        drops,
                        dropRates
                ));
            }
        }

        registration.addRecipes(PokemonDropCategory.TYPE, recipes);
        System.out.println("[JustEnoughCobblemon] " + recipes.size() + " recettes chargées.");
    }
}