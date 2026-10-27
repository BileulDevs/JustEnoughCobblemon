package dev.darcosse.common.justenoughcobblemon.recipes;

import net.minecraft.world.item.ItemStack;

/**
 * One item a Pokémon can drop, carrying everything the JEI page needs to describe it.
 * <p>
 * The chance and quantity travel with the stack on purpose: they used to live in a separate
 * {@code Map<ItemStack, Float>}, and {@link ItemStack} has no value-based {@code equals}, so any copy
 * of the stack (by JEI or by us) silently lost its drop rate.
 *
 * @param stack       the stack shown in the slot; its count is the lowest quantity that can drop
 * @param chance      Cobblemon's {@code percentage}: the chance the entry is selected, in percent.
 *                    It is not the chance of receiving the item, see {@link #chanceOfAtLeastOne()}
 * @param minQuantity lowest quantity dropped in one roll
 * @param maxQuantity highest quantity dropped in one roll; equals {@code minQuantity} for a fixed amount
 * @author Darcosse
 * @version 1.2
 * @since 2026
 */
public record PokemonDrop(
        ItemStack stack,
        float chance,
        int minQuantity,
        int maxQuantity
) {

    /**
     * Chance, in percent, that the player actually receives at least one item from this entry.
     * <p>
     * Cobblemon first selects the entry with {@link #chance}, then rolls the quantity uniformly in
     * {@code [minQuantity, maxQuantity]} ({@code ItemDropEntry.drop}). A range starting at 0 can therefore
     * select the entry and still drop nothing: Silcoon's "0-2" string is selected 100% of the time but
     * yields string only 2 times out of 3.
     * <p>
     * This ignores the drop table's {@code amount} budget, which only matters when several entries
     * compete for it; simulating it would need every entry of the table and is not worth the complexity.
     */
    public float chanceOfAtLeastOne() {
        int rangeSize = maxQuantity - minQuantity + 1;
        if (rangeSize <= 0) return 0f;

        // Values below 1 drop nothing; only the part of the range from max(min, 1) upwards gives items.
        int rewardingValues = maxQuantity - Math.max(minQuantity, 1) + 1;
        if (rewardingValues <= 0) return 0f;

        return chance * rewardingValues / rangeSize;
    }
}
