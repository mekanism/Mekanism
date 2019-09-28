package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class ChemicalInfuserIRecipe extends ChemicalInfuserRecipe {

    public ChemicalInfuserIRecipe(ResourceLocation id, GasStackIngredient leftInput, GasStackIngredient rightInput, Gas outputGas, int outputGasAmount) {
        super(id, leftInput, rightInput, outputGas, outputGasAmount);
    }

    @Nonnull
    @Override
    public IRecipeType<ChemicalInfuserRecipe> getType() {
        return MekanismRecipeType.CHEMICAL_INFUSING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ChemicalInfuserRecipe> getSerializer() {
        return MekanismRecipeSerializers.CHEMICAL_INFUSING;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.CHEMICAL_INFUSER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.CHEMICAL_INFUSER.getItemStack();
    }
}