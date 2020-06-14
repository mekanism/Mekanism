package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class PressurizedReactionIRecipe extends PressurizedReactionRecipe {

    public PressurizedReactionIRecipe(ResourceLocation id, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas,
          FloatingLong energyRequired, int duration, ItemStack outputItem, GasStack outputGas) {
        super(id, inputSolid, inputFluid, inputGas, energyRequired, duration, outputItem, outputGas);
    }

    @Nonnull
    @Override
    public IRecipeType<PressurizedReactionRecipe> getType() {
        return MekanismRecipeType.REACTION;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<PressurizedReactionRecipe> getSerializer() {
        return MekanismRecipeSerializers.REACTION.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.PRESSURIZED_REACTION_CHAMBER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.PRESSURIZED_REACTION_CHAMBER.getItemStack();
    }
}