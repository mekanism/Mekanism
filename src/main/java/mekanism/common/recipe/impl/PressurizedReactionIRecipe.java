package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class PressurizedReactionIRecipe extends PressurizedReactionRecipe {

    public PressurizedReactionIRecipe(ResourceLocation id, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient gasInput, Gas outputGas,
          int outputGasAmount, double energyRequired, int duration, ItemStack output) {
        super(id, inputSolid, inputFluid, gasInput, outputGas, outputGasAmount, energyRequired, duration, output);
    }

    @Nonnull
    @Override
    public IRecipeType<PressurizedReactionRecipe> getType() {
        return MekanismRecipeType.REACTION;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<PressurizedReactionRecipe> getSerializer() {
        return MekanismRecipeSerializers.REACTION;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.PRESSURIZED_REACTION_CHAMBER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.PRESSURIZED_REACTION_CHAMBER.getItemStack();
    }
}