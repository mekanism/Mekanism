package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ChemicalToItemStackRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class GasToItemStackRecipe extends ChemicalToItemStackRecipe<Gas, GasStack, GasStackIngredient> {

    public GasToItemStackRecipe(ResourceLocation id, GasStackIngredient input, ItemStack output) {
        super(id, input, output);
    }
}