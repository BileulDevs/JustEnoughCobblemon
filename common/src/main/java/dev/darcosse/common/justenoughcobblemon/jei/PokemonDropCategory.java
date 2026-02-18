package dev.darcosse.common.justenoughcobblemon.jei;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import dev.darcosse.common.justenoughcobblemon.recipes.PokemonDropRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * JEI category implementation for displaying Pokémon drop tables.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
public class PokemonDropCategory implements IRecipeCategory<PokemonDropRecipe> {
    public static final RecipeType<PokemonDropRecipe> TYPE =
            RecipeType.create("justenoughcobblemon", "pokemon_drops", PokemonDropRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public PokemonDropCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 60);
        this.icon = guiHelper.createDrawableItemStack(CobblemonItems.POKE_BALL.getDefaultInstance());
    }

    /**
     * Handles the visual rendering of the recipe (Pokémon model).
     */
    @Override
    public void draw(PokemonDropRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        PokemonRenderer.INSTANCE.render(graphics, recipe.pokemonName(), 28, 0);
    }

    /**
     * Configures the layout of the recipe slots and their tooltips.
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PokemonDropRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> drops = recipe.drops();
        if (drops.isEmpty()) return;

        int columns = 3;
        int slotSize = 18;
        int gapX = 22;
        int gapY = 22;

        int rowCount = (int) Math.ceil((double) drops.size() / columns);
        int totalContentHeight = (rowCount * gapY) - (gapY - slotSize);
        int startY = (60 - totalContentHeight) / 2;
        int startX = 70;

        for (int i = 0; i < drops.size(); i++) {
            ItemStack stack = drops.get(i);
            int x = startX + (i % columns) * gapX;
            int y = startY + (i / columns) * gapY;

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(stack)
                    .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                        Float rate = recipe.dropRates().get(stack);
                        if (rate != null) {
                            tooltip.add(Component.literal(rate + "%").withStyle(ChatFormatting.GREEN));
                        }
                    });
        }
    }

    /**
     * Handles tooltip display when hovering over the Pokémon model area.
     */
    @Override
    public void getTooltip(ITooltipBuilder tooltip, PokemonDropRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= 4 && mouseX <= 44 && mouseY >= 0 && mouseY <= 55) {
            var species = PokemonSpecies.getByName(recipe.pokemonName().toLowerCase());
            if (species != null) {
                tooltip.add(species.getTranslatedName().withStyle(ChatFormatting.AQUA));
            } else {
                tooltip.add(Component.literal(recipe.pokemonName()));
            }
        }
    }

    @Override public @NotNull RecipeType<PokemonDropRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.literal("Cobblemon Drops"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }
}