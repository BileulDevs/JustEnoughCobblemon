package dev.darcosse.common.justenoughcobblemon.jei;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import dev.darcosse.common.justenoughcobblemon.recipes.PokemonDrop;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * JEI category showing what a Pokémon drops: the 3D model on the left, the drop grid on the right.
 *
 * @author Darcosse
 * @version 1.5
 * @since 2026
 */
public class PokemonDropCategory implements IRecipeCategory<PokemonDropRecipe> {
    public static final RecipeType<PokemonDropRecipe> TYPE =
            RecipeType.create("justenoughcobblemon", "pokemon_drops", PokemonDropRecipe.class);

    private static final String TITLE_KEY = "justenoughcobblemon.jei.category.pokemon_drops";

    /**
     * Prefix of Cobblemon's own form labels ("Alolan Form", "Heat Rotom", ...), so form names come translated
     * in every language Cobblemon ships without adding keys to this mod. Cobblemon's Pokédex builds the full
     * key as prefix + species showdown id + "-" + lowercase form name without dashes (PokemonInfoWidget,
     * 1.8.4); all 77 forms with their own drops have such a key in Cobblemon's en_us.json.
     */
    private static final String COBBLEMON_FORM_KEY_PREFIX = "cobblemon.ui.pokedex.info.form.";

    /**
     * Mod font holding a single 4x4 px "×" (assets/justenoughcobblemon/font/small_multiply.json), used only
     * for the quantity line. The default-font "×" is the same size as the letter x, so "×0-2" read as
     * "x0-2"; the vanilla Unicode font was smaller but too thin and off-style. The font only maps "×":
     * any other character given this font renders as a missing-glyph box.
     */
    private static final ResourceLocation SMALL_GLYPH_FONT =
            ResourceLocation.fromNamespaceAndPath("justenoughcobblemon", "small_multiply");

    /** 10 keeps one decimal in displayed chances; 100 would keep two, 1 would round to whole percents. */
    private static final float CHANCE_DECIMAL_FACTOR = 10f;

    /** Category size in pixels. Changing the height shifts the grid's vertical centring and the model area. */
    private static final int WIDTH = 150;
    private static final int HEIGHT = 60;

    /** Columns of the drop grid. Wider grids must keep {@code GRID_START_X + columns * SLOT_PITCH <= WIDTH}. */
    private static final int GRID_COLUMNS = 3;

    /** Rows of the drop grid. More than 3 rows of {@link #SLOT_PITCH} no longer fit in {@link #HEIGHT}. */
    private static final int GRID_ROWS = 3;

    /** Number of drops one page can hold; the plugin paginates drop tables with this value. */
    public static final int GRID_CAPACITY = GRID_COLUMNS * GRID_ROWS;

    /**
     * Distance between two slot origins. 18 is the vanilla slot frame size, so frames tile without gaps;
     * a larger pitch leaves visible gaps between frames and three rows stop fitting in {@link #HEIGHT}.
     */
    private static final int SLOT_PITCH = 18;

    /** JEI positions the 16 px item, while its 18 px frame starts one pixel up and to the left. */
    private static final int SLOT_FRAME_INSET = 1;

    /** Left edge of the grid; everything to its left is reserved for the Pokémon model. */
    private static final int GRID_START_X = 70;

    /** Where the model is anchored, and the hover box that shows the species name. */
    private static final int MODEL_X = 28;
    private static final int MODEL_Y = 0;
    private static final int MODEL_HOVER_MIN_X = 4;
    private static final int MODEL_HOVER_MAX_X = 44;
    private static final int MODEL_HOVER_MIN_Y = 0;
    private static final int MODEL_HOVER_MAX_Y = 55;

    private final IDrawable icon;

    /**
     * Prepares the drawables once; JEI keeps a single category instance for the whole session.
     */
    public PokemonDropCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(CobblemonItems.POKE_BALL.getDefaultInstance());
    }

    /**
     * Draws the Pokémon model behind the slots. Called every frame while the page is visible.
     */
    @Override
    public void draw(PokemonDropRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        PokemonRenderer.INSTANCE.render(graphics, recipe.speciesId(), recipe.aspects(), MODEL_X, MODEL_Y);
    }

    /**
     * Lays out the page's drops as a framed grid vertically centred in the category.
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PokemonDropRecipe recipe, IFocusGroup focuses) {
        List<PokemonDrop> drops = recipe.drops();
        if (drops.isEmpty()) return;

        int rowCount = (drops.size() + GRID_COLUMNS - 1) / GRID_COLUMNS;
        int startY = (HEIGHT - rowCount * SLOT_PITCH) / 2 + SLOT_FRAME_INSET;
        int startX = GRID_START_X + SLOT_FRAME_INSET;

        for (int i = 0; i < drops.size(); i++) {
            PokemonDrop drop = drops.get(i);
            int x = startX + (i % GRID_COLUMNS) * SLOT_PITCH;
            int y = startY + (i / GRID_COLUMNS) * SLOT_PITCH;

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setStandardSlotBackground()
                    .addItemStack(drop.stack())
                    // The data is read from the captured record, never looked up by stack: JEI may copy stacks.
                    .addRichTooltipCallback((recipeSlotView, tooltip) -> addDropTooltip(tooltip, drop));
        }
    }

    /**
     * Appends the chance of receiving the item and, when the amount varies, the quantity range to a
     * slot tooltip. Both lines are numbers and symbols only, so they need no translation key.
     */
    private static void addDropTooltip(ITooltipBuilder tooltip, PokemonDrop drop) {
        // Cobblemon's raw percentage only says the entry is selected; with a range starting at 0 it
        // showed "100%" for drops that give nothing one time out of three.
        tooltip.add(Component.literal(formatChance(drop.chanceOfAtLeastOne())).withStyle(ChatFormatting.GREEN));
        if (drop.minQuantity() != drop.maxQuantity()) {
            tooltip.add(Component.empty()
                    .append(Component.literal("×").withStyle(style -> style.withFont(SMALL_GLYPH_FONT)))
                    .append(Component.literal(drop.minQuantity() + "–" + drop.maxQuantity()))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * Formats a percentage with at most one decimal, dropping a useless ".0" (100 shows as "100%",
     * 66.666 as "66.7%", 99.96 as "100%"). The rounding happens before the integer test so that a value
     * just below a whole number does not print as "100.0%".
     * {@link Locale#ROOT} is deliberate: the JVM locale does not follow the game language, so a
     * locale-dependent decimal separator would differ between players regardless of their game settings.
     */
    private static String formatChance(float chance) {
        float rounded = Math.round(chance * CHANCE_DECIMAL_FACTOR) / CHANCE_DECIMAL_FACTOR;
        if (rounded == Math.rint(rounded)) {
            return String.format(Locale.ROOT, "%d%%", (int) rounded);
        }
        return String.format(Locale.ROOT, "%.1f%%", rounded);
    }

    /**
     * Shows the species name, and the form name for form pages, when hovering the model area.
     */
    @Override
    public void getTooltip(ITooltipBuilder tooltip, PokemonDropRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        boolean overModel = mouseX >= MODEL_HOVER_MIN_X && mouseX <= MODEL_HOVER_MAX_X
                && mouseY >= MODEL_HOVER_MIN_Y && mouseY <= MODEL_HOVER_MAX_Y;
        if (!overModel) return;

        var species = PokemonSpecies.getByIdentifier(recipe.speciesId());
        if (species != null) {
            tooltip.add(species.getTranslatedName().withStyle(ChatFormatting.AQUA));
            String formName = recipe.formName();
            if (formName != null) {
                tooltip.add(formLabel(species.showdownId(), formName).withStyle(ChatFormatting.GRAY));
            }
        } else {
            // Species can disappear between JEI reloads (datapack removed); the raw id still tells the player what it was.
            tooltip.add(Component.literal(recipe.speciesId().toString()));
        }
    }

    /**
     * Builds the translated label of a form, falling back to its raw internal name for forms that
     * Cobblemon has no label for (typically forms added by datapacks or other mods).
     */
    private static MutableComponent formLabel(String speciesShowdownId, String formName) {
        String key = COBBLEMON_FORM_KEY_PREFIX + speciesShowdownId + "-"
                + formName.toLowerCase(Locale.ROOT).replace("-", "");
        return Component.translatableWithFallback(key, formName);
    }

    /** Ties recipes registered by the plugin to this category. */
    @Override public @NotNull RecipeType<PokemonDropRecipe> getRecipeType() { return TYPE; }

    /** Category tab title, translated in every language file of the mod. */
    @Override public @NotNull Component getTitle() { return Component.translatable(TITLE_KEY); }

    /** Category width; replaces the blank background drawable deprecated since JEI 19.19. */
    @Override public int getWidth() { return WIDTH; }

    /** Category height; replaces the blank background drawable deprecated since JEI 19.19. */
    @Override public int getHeight() { return HEIGHT; }

    /** Tab icon. */
    @Override public IDrawable getIcon() { return icon; }
}
