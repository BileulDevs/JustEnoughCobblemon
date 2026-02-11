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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PokemonDropCategory implements IRecipeCategory<PokemonDropRecipe> {
    public static final RecipeType<PokemonDropRecipe> TYPE =
            RecipeType.create("justenoughcobblemon", "pokemon_drops", PokemonDropRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public PokemonDropCategory(IGuiHelper guiHelper) {
        // On garde un fond vide, ou tu peux utiliser une texture de plateforme
        this.background = guiHelper.createBlankDrawable(150, 60);
        this.icon = guiHelper.createDrawableItemStack(CobblemonItems.POKE_BALL.getDefaultInstance());
    }

    @Override
    public void draw(PokemonDropRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        PokemonRenderer.INSTANCE.render(graphics, recipe.pokemonName(), 24, 0);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PokemonDropRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> drops = recipe.drops();

        int startX = 70;
        int startY = 15;
        int gapX = 22;
        int gapY = 22;
        int columns = 3;

        for (int i = 0; i < drops.size(); i++) {
            ItemStack stack = drops.get(i);

            int x = startX + (i % columns) * gapX;
            int y = startY + (i / columns) * gapY;

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(stack)
                    .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                        Float rate = recipe.dropRates().get(stack);
                        if (rate != null) {
                            tooltip.add(Component.literal("§a" + rate + "%"));
                        }
                    });
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, PokemonDropRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int zoneX = 4;
        int zoneY = 0;
        int zoneWidth = 40;
        int zoneHeight = 55;

        if (mouseX >= zoneX && mouseX <= (zoneX + zoneWidth) && mouseY >= zoneY && mouseY <= (zoneY + zoneHeight)) {
            var species = PokemonSpecies.getByName(recipe.pokemonName().toLowerCase());
            if (species != null) {
                tooltip.add(species.getTranslatedName().withStyle(net.minecraft.ChatFormatting.AQUA));
            } else {
                tooltip.add(Component.literal(recipe.pokemonName()));
            }
        }
    }

    @Override public RecipeType<PokemonDropRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.literal("Cobblemon Drops"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }
}