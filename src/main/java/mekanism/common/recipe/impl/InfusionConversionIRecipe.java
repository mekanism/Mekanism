package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.basic.BasicItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class InfusionConversionIRecipe extends BasicItemStackToInfuseTypeRecipe implements ChemicalOutputInternal<InfuseType, InfusionStack> {

    public InfusionConversionIRecipe(ItemStackIngredient input, InfusionStack output) {
        super(input, output);
    }

    @Override
    public RecipeSerializer<InfusionConversionIRecipe> getSerializer() {
        return MekanismRecipeSerializers.INFUSION_CONVERSION.get();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.METALLURGIC_INFUSER.getItemStack();
    }

    @Override
    public InfusionStack getOutputRaw() {
        return output;
    }
}