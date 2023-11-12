package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class PressurizedReactionIRecipe extends BasicPressurizedReactionRecipe {

    public PressurizedReactionIRecipe(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas,
          FloatingLong energyRequired, int duration, ItemStack outputItem, GasStack outputGas) {
        super(inputSolid, inputFluid, inputGas, energyRequired, duration, outputItem, outputGas);
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    public GasStack getOutputGas() {
        return outputGas;
    }

    @Override
    public RecipeSerializer<PressurizedReactionIRecipe> getSerializer() {
        return MekanismRecipeSerializers.REACTION.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.PRESSURIZED_REACTION_CHAMBER.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PRESSURIZED_REACTION_CHAMBER.getItemStack();
    }
}