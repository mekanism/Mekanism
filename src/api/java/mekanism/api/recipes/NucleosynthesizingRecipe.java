package mekanism.api.recipes;

import mekanism.api.MekanismAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Extension of [ItemStackChemicalToItemStackRecipe] with a defined amount of ticks needed to process. Input: ItemStack
///
/// Input: Chemical (Base value, will be multiplied by a per tick amount)
///
/// Output: ItemStack
///
/// @apiNote Nucleosynthesizers can process this recipe type.
public abstract class NucleosynthesizingRecipe extends ItemStackChemicalToItemStackRecipe {

    private static final Holder<Item> ANTIPROTONIC_NUCLEOSYNTHESIZER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "antiprotonic_nucleosynthesizer"));

    @Override
    public final RecipeType<NucleosynthesizingRecipe> getType() {
        return MekanismRecipeTypes.TYPE_NUCLEOSYNTHESIZING.value();
    }

    /// Gets the duration in ticks this recipe takes to complete.
    public abstract int getDuration();

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ANTIPROTONIC_NUCLEOSYNTHESIZER);
    }
}
