package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.serializer.RotaryRecipeSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class RotaryIRecipe extends RotaryRecipe {

    private RotaryIRecipe(ResourceLocation id, FluidStackIngredient fluidInput, GasStack gasOutput) {
        super(id, fluidInput, gasOutput);
    }

    private RotaryIRecipe(ResourceLocation id, GasStackIngredient gasInput, FluidStack fluidOutput) {
        super(id, gasInput, fluidOutput);
    }

    private RotaryIRecipe(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput) {
        super(id, fluidInput, gasInput, gasOutput, fluidOutput);
    }

    @Nonnull
    @Override
    public IRecipeType<RotaryRecipe> getType() {
        return MekanismRecipeType.ROTARY;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<RotaryRecipe> getSerializer() {
        return MekanismRecipeSerializers.ROTARY.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.ROTARY_CONDENSENTRATOR.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.ROTARY_CONDENSENTRATOR.getItemStack();
    }

    public static class Factory implements RotaryRecipeSerializer.IFactory<RotaryRecipe> {

        @Override
        public RotaryRecipe create(ResourceLocation id, FluidStackIngredient fluidInput, GasStack gasOutput) {
            return new RotaryIRecipe(id, fluidInput, gasOutput);
        }

        @Override
        public RotaryRecipe create(ResourceLocation id, GasStackIngredient gasInput, FluidStack fluidOutput) {
            return new RotaryIRecipe(id, gasInput, fluidOutput);
        }

        @Override
        public RotaryRecipe create(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput) {
            return new RotaryIRecipe(id, fluidInput, gasInput, gasOutput, fluidOutput);
        }
    }
}