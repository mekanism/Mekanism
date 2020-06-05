package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToInfuseTypeRecipe extends ItemStackToChemicalRecipe<InfuseType, InfusionStack> {

    public ItemStackToInfuseTypeRecipe(ResourceLocation id, ItemStackIngredient input, InfusionStack output) {
        super(id, input, output);
    }

    @Override
    public InfusionStack getOutput(ItemStack input) {
        return output.copy();
    }
}