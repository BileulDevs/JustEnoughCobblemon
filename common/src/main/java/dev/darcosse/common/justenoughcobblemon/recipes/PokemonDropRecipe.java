package dev.darcosse.common.justenoughcobblemon.recipes;

import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Map;

public record PokemonDropRecipe(
        String pokemonName,
        List<ItemStack> drops,
        Map<ItemStack, Float> dropRates
) {}