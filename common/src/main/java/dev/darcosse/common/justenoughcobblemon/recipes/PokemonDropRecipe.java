package dev.darcosse.common.justenoughcobblemon.recipes;

import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Map;

/**
 * Data record representing a Pokémon drop recipe for JEI.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
public record PokemonDropRecipe(
        String pokemonName,
        List<ItemStack> drops,
        Map<ItemStack, Float> dropRates
) {}