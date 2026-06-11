package mekanism.api.recipes;

import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/// Base class for defining item chemical to item recipes.
///
/// Input: ItemStack
///
/// Input: Chemical
///
/// Output: ItemStack
///
/// @apiNote There are currently six types of ItemStack Chemical to ItemStack recipe types:
/// - Compressing: Can be processed in Osmium Compressors and Compressing Factories.
/// - Injecting: Can be processed in Chemical Injection Chambers and Injecting Factories.
/// - Purifying: Can be processed in Purification Chambers and Purifying Factories.
/// - Infusing: Can be processed in Metallurgic Infusers and Infusing Factories.
/// - Painting: Can be processed in Painting Machines.
/// - Nucleosynthesizing: Can be processed in the Antiprotonic Nucleosynthesizer.
public abstract class ItemStackChemicalToItemStackRecipe extends ItemStackChemicalToObjectRecipe<ItemStackTemplate> {

    @Override
    public ItemStack assemble(SingleItemChemicalRecipeInput input) {
        if (!isIncomplete() && test(input.item(), input.chemical())) {
            return getOutput(input.item(), input.chemical()).create();
        }
        return ItemStack.EMPTY;
    }
}