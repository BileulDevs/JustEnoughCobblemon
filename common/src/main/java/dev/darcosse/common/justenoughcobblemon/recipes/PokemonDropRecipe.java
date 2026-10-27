package dev.darcosse.common.justenoughcobblemon.recipes;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * One JEI page of the "Pokémon drops" category: a species, optionally one of its forms, and the slice of
 * the matching drop table shown on that page.
 * <p>
 * A drop table with more drops than fit on a page produces several recipes sharing the same species and form.
 *
 * @param speciesId full species identifier (namespace included), so species added by datapacks or other mods
 *                  resolve correctly; a bare showdown id only resolves inside the {@code cobblemon} namespace
 * @param formName  internal name of the form ("Alola", "Heat", ...) when the page shows a form's own drop
 *                  table, {@code null} for the base species
 * @param aspects   aspects that make Cobblemon pick the form's model; empty for the base species
 * @param drops     drops displayed on this page, already paginated and immutable
 * @author Darcosse
 * @version 1.2
 * @since 2026
 */
public record PokemonDropRecipe(
        ResourceLocation speciesId,
        @Nullable String formName,
        Set<String> aspects,
        List<PokemonDrop> drops
) {}
