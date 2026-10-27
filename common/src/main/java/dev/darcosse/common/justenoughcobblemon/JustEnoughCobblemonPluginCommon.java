package dev.darcosse.common.justenoughcobblemon;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.drop.DropEntry;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.common.collect.Lists;
import dev.darcosse.common.justenoughcobblemon.jei.PokemonDropCategory;
import dev.darcosse.common.justenoughcobblemon.recipes.PokemonDrop;
import dev.darcosse.common.justenoughcobblemon.recipes.PokemonDropRecipe;
import kotlin.ranges.IntRange;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Loader-agnostic JEI plugin: declares the "Pokémon drops" category and turns every Cobblemon species
 * drop table into JEI recipes. The Fabric and NeoForge plugins only exist to carry {@code @JeiPlugin}.
 *
 * @author Darcosse
 * @version 1.4
 * @since 2026
 */
public abstract class JustEnoughCobblemonPluginCommon implements IModPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("JustEnoughCobblemon");

    /**
     * Drops shown on one JEI page. Must stay equal to the grid capacity of {@link PokemonDropCategory}
     * (columns x rows); raising it without enlarging the grid makes slots overflow the category bounds.
     */
    public static final int DROPS_PER_PAGE = PokemonDropCategory.GRID_CAPACITY;

    /**
     * Lowest stack count put in a slot. A drop range may start at 0 ("0 to 2"), and an
     * {@code ItemStack} with count 0 is empty, which JEI renders as a blank slot.
     */
    private static final int MIN_DISPLAYED_COUNT = 1;

    /**
     * National Pokédex order, then identifier. The tie-breaker keeps the order stable between reloads
     * for species sharing a number, which datapack and addon species can do.
     */
    private static final Comparator<Species> POKEDEX_ORDER = Comparator
            .comparingInt(Species::getNationalPokedexNumber)
            .thenComparing(species -> species.getResourceIdentifier().toString());

    /**
     * Identifies this plugin to JEI; changing it makes JEI treat the plugin as a new one and drop
     * the player's per-plugin settings such as hidden categories.
     */
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("justenoughcobblemon", "jei_plugin");
    }

    /**
     * Declares the "Pokémon drops" category so JEI has somewhere to put the recipes built below.
     */
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PokemonDropCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    /**
     * Lets players open the category by pressing the "show uses" key (U by default) on a Poké Ball or on
     * any Pokédex, and lists those items as the category's catalysts. All Pokédex colours are registered
     * because players hold whichever colour they crafted.
     */
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        List<ItemLike> catalysts = new ArrayList<>();
        catalysts.add(CobblemonItems.POKE_BALL);
        catalysts.addAll(CobblemonItems.INSTANCE.getPokedexes());
        registration.addRecipeCatalysts(PokemonDropCategory.TYPE, catalysts.toArray(ItemLike[]::new));
    }

    /**
     * Builds one or more JEI pages per species that has at least one displayable item drop, followed by
     * pages for each form that declares its own drop table (Alolan Rattata, Rotom appliances, ...).
     * Runs on the client each time JEI reloads (joining a world, reloading resources), not every tick.
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<PokemonDropRecipe> recipes = new ArrayList<>();

        // getSpecies() guarantees no order (Wurmple showed up after Silcoon); JEI shows recipes in
        // registration order. Unimplemented species are kept on purpose: Cobblemon renders its fallback model.
        List<Species> orderedSpecies = new ArrayList<>(PokemonSpecies.getSpecies());
        orderedSpecies.sort(POKEDEX_ORDER);

        for (Species species : orderedSpecies) {
            addPages(recipes, species, null, Set.of(), species.getDrops());

            for (FormData form : species.getForms()) {
                // FormData.getDrops() falls back to the species table when the form declares none (read from
                // the 1.8.4 bytecode), so an identity check is exactly "this form has its own drops".
                if (form.getDrops() == species.getDrops()) continue;
                addPages(recipes, species, form.getName(), Set.copyOf(form.getAspects()), form.getDrops());
            }
        }

        registration.addRecipes(PokemonDropCategory.TYPE, recipes);
    }

    /**
     * Turns one drop table into as many JEI pages as needed and appends them to {@code recipes}.
     * Tables without any displayable item produce no page at all.
     *
     * @param formName {@code null} for the base species, otherwise the form's internal name
     * @param aspects  aspects selecting the form's model; empty for the base species
     */
    private static void addPages(List<PokemonDropRecipe> recipes, Species species, @Nullable String formName,
                                 Set<String> aspects, DropTable table) {
        List<PokemonDrop> drops = collectItemDrops(species, table);
        for (List<PokemonDrop> page : Lists.partition(drops, DROPS_PER_PAGE)) {
            // Lists.partition returns live views of the backing list; copying detaches each page from it.
            recipes.add(new PokemonDropRecipe(species.getResourceIdentifier(), formName, aspects, List.copyOf(page)));
        }
    }

    /**
     * Extracts the item drops of a table, skipping non-item entries (commands, etc.)
     * and items that cannot be resolved in the current game.
     */
    private static List<PokemonDrop> collectItemDrops(Species species, DropTable table) {
        List<PokemonDrop> drops = new ArrayList<>();
        for (DropEntry entry : table.getEntries()) {
            if (!(entry instanceof ItemDropEntry itemDrop)) continue;

            PokemonDrop drop = toDisplayDrop(species, itemDrop);
            if (drop != null) {
                drops.add(drop);
            }
        }
        return drops;
    }

    /**
     * Converts a Cobblemon drop entry into what JEI displays, or returns {@code null} when the item
     * does not exist (optional mod missing, typo in a datapack).
     */
    private static @Nullable PokemonDrop toDisplayDrop(Species species, ItemDropEntry itemDrop) {
        ResourceLocation itemId = itemDrop.getItem();
        if (itemId == null) return null;

        // Registry.get falls back to AIR for unknown ids, which used to produce an empty slot with a drop rate.
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
        if (item.isEmpty() || item.get() == Items.AIR) {
            LOGGER.warn("Skipping drop '{}' of species '{}': item is not registered",
                    itemId, species.getResourceIdentifier());
            return null;
        }

        // Cobblemon rolls quantityRange when it is set and ignores quantity entirely in that case.
        IntRange range = itemDrop.getQuantityRange();
        int minQuantity = range != null ? range.getFirst() : itemDrop.getQuantity();
        int maxQuantity = range != null ? range.getLast() : itemDrop.getQuantity();

        ItemStack stack = new ItemStack(item.get(), Math.max(MIN_DISPLAYED_COUNT, minQuantity));
        return new PokemonDrop(stack, itemDrop.getPercentage(), minQuantity, maxQuantity);
    }
}
