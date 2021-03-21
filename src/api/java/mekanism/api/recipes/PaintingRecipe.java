package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PaintingRecipe extends ItemStackChemicalToItemStackRecipe<Pigment, PigmentStack, PigmentStackIngredient> {

    public PaintingRecipe(ResourceLocation id, ItemStackIngredient itemInput, PigmentStackIngredient pigmentInput, ItemStack output) {
        super(id, itemInput, pigmentInput, output);
    }
}