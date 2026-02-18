package dev.darcosse.common.justenoughcobblemon;

import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.common.collect.Lists;
import dev.darcosse.common.justenoughcobblemon.jei.PokemonDropCategory;
import dev.darcosse.common.justenoughcobblemon.recipes.PokemonDropRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main JEI plugin class for Cobblemon drop integration.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
public abstract class JustEnoughCobblemonPluginCommon implements IModPlugin {

    /**
     * Gets the unique identifier for this JEI plugin.
     * @return The plugin's ResourceLocation.
     */
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("justenoughcobblemon", "jei_plugin");
    }

    /**
     * Registers the custom recipe categories for this plugin.
     * @param registration The category registration handler.
     */
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PokemonDropCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    /**
     * Scans Cobblemon species to register drop recipes in JEI.
     * @param registration The recipe registration handler.
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<PokemonDropRecipe> recipes = new ArrayList<>();
        int itemsPerPage = 9;

        for (Species species : PokemonSpecies.getSpecies()) {
            List<ItemStack> allDrops = new ArrayList<>();
            Map<ItemStack, Float> dropRates = new HashMap<>();

            species.getDrops().getEntries().forEach(dropData -> {
                if (dropData instanceof ItemDropEntry itemDrop) {
                    ResourceLocation itemRes = itemDrop.getItem();
                    if (itemRes != null) {
                        var item = BuiltInRegistries.ITEM.get(itemRes);
                        ItemStack stack = new ItemStack(item, itemDrop.getQuantity());

                        allDrops.add(stack);
                        dropRates.put(stack, itemDrop.getPercentage());
                    }
                }
            });

            if (!allDrops.isEmpty()) {
                List<List<ItemStack>> pages = Lists.partition(allDrops, itemsPerPage);

                for (List<ItemStack> page : pages) {
                    recipes.add(new PokemonDropRecipe(
                            species.showdownId(),
                            new ArrayList<>(page),
                            dropRates
                    ));
                }
            }
        }

        registration.addRecipes(PokemonDropCategory.TYPE, recipes);
    }
}