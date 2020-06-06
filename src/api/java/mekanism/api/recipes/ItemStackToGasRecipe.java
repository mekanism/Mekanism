package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToGasRecipe extends ItemStackToChemicalRecipe<Gas, GasStack> {

    public ItemStackToGasRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
        super(id, input, output);
    }

    @Override
    public GasStack getOutput(ItemStack input) {
        return output.copy();
    }
}