package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class PressurizedReactionIRecipe extends PressurizedReactionRecipe {

    public PressurizedReactionIRecipe(ResourceLocation id, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas,
          FloatingLong energyRequired, int duration, ItemStack outputItem, GasStack outputGas) {
        super(id, inputSolid, inputFluid, inputGas, energyRequired, duration, outputItem, outputGas);
    }

    @Nonnull
    @Override
    public RecipeType<PressurizedReactionRecipe> getType() {
        return MekanismRecipeType.REACTION.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<PressurizedReactionRecipe> getSerializer() {
        return MekanismRecipeSerializers.REACTION.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.PRESSURIZED_REACTION_CHAMBER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PRESSURIZED_REACTION_CHAMBER.getItemStack();
    }
}