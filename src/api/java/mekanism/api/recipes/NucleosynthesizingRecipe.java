package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.display.NucleosynthesizingRecipeDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Extension of [ItemStackChemicalToItemStackRecipe] with a defined amount of ticks needed to process.
///
/// Input: ItemStack
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
    public List<RecipeDisplay> display() {
        return List.of(new NucleosynthesizingRecipeDisplay(
              getItemInput().display(),
              getChemicalInput().display(),
              perTickUsage(),
              getDuration(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(ANTIPROTONIC_NUCLEOSYNTHESIZER)
        ));
    }
}
