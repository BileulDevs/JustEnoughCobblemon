package dev.darcosse.common.justenoughcobblemon.jei;

import com.cobblemon.mod.common.CobblemonItems;
import dev.darcosse.common.justenoughcobblemon.recipes.PokemonDropRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PokemonDropCategory implements IRecipeCategory<PokemonDropRecipe> {
    public static final RecipeType<PokemonDropRecipe> TYPE =
            RecipeType.create("justenoughcobblemon", "pokemon_drops", PokemonDropRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public PokemonDropCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 50);
        this.icon = guiHelper.createDrawableItemStack(CobblemonItems.POKE_BALL.getDefaultInstance());
    }

    @Override public RecipeType<PokemonDropRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.literal("Cobblemon Drops"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PokemonDropRecipe recipe, IFocusGroup focuses) {
        // Slot d'entrée (Pokémon)
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 15)
                .addItemStack(CobblemonItems.POKE_BALL.getDefaultInstance())
                .addRichTooltipCallback((recipeSlotView, tooltip) ->
                        tooltip.add(Component.literal("§6Pokémon: §f" + recipe.pokemonName())));

        // Slots de sortie (Drops)
        int x = 50;
        for (ItemStack stack : recipe.drops()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, 15)
                    .addItemStack(stack)
                    .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                        Float rate = recipe.dropRates().get(stack);
                        if (rate != null) {
                            // Affiche "Taux de drop: 100.0%"
                            tooltip.add(Component.literal("§7Taux de drop: §a" + rate + "%"));
                        }
                    });
            x += 20;
        }
    }
}