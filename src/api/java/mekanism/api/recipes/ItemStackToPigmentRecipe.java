package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToPigmentRecipe extends ItemStackToChemicalRecipe<Pigment, PigmentStack> {

    public ItemStackToPigmentRecipe(ResourceLocation id, ItemStackIngredient input, PigmentStack output) {
        super(id, input, output);
    }

    @Override
    public PigmentStack getOutput(ItemStack input) {
        return output.copy();
    }
}