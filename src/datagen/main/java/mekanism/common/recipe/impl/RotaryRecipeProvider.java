package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.api.datagen.recipe.builder.RotaryRecipeBuilder;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;

class RotaryRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "rotary/";
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.BRINE, MekanismFluids.BRINE, MekanismTags.Fluids.BRINE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.CHLORINE, MekanismFluids.CHLORINE, MekanismTags.Fluids.CHLORINE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.ETHENE, MekanismFluids.ETHENE, MekanismTags.Fluids.ETHENE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.HYDROGEN, MekanismFluids.HYDROGEN, MekanismTags.Fluids.HYDROGEN);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.HYDROGEN_CHLORIDE, MekanismFluids.HYDROGEN_CHLORIDE, MekanismTags.Fluids.HYDROGEN_CHLORIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.LITHIUM, MekanismFluids.LITHIUM, MekanismTags.Fluids.LITHIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.OXYGEN, MekanismFluids.OXYGEN, MekanismTags.Fluids.OXYGEN);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SODIUM, MekanismFluids.SODIUM, MekanismTags.Fluids.SODIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.STEAM, MekanismFluids.STEAM, MekanismTags.Fluids.STEAM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SULFUR_DIOXIDE, MekanismFluids.SULFUR_DIOXIDE, MekanismTags.Fluids.SULFUR_DIOXIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SULFUR_TRIOXIDE, MekanismFluids.SULFUR_TRIOXIDE, MekanismTags.Fluids.SULFUR_TRIOXIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SULFURIC_ACID, MekanismFluids.SULFURIC_ACID, MekanismTags.Fluids.SULFURIC_ACID);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.HYDROFLUORIC_ACID, MekanismFluids.HYDROFLUORIC_ACID, MekanismTags.Fluids.HYDROFLUORIC_ACID);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.WATER_VAPOR, new IFluidProvider() {
            @Nonnull
            @Override
            public Fluid getFluid() {
                return Fluids.WATER;
            }
        }, FluidTags.WATER);
    }

    private void addRotaryCondensentratorRecipe(Consumer<IFinishedRecipe> consumer, String basePath, IGasProvider gas, IFluidProvider fluidOutput, ITag<Fluid> fluidInput) {
        RotaryRecipeBuilder.rotary(
              FluidStackIngredient.from(fluidInput, 1),
              GasStackIngredient.from(gas, 1),
              gas.getStack(1),
              fluidOutput.getFluidStack(1)
        ).build(consumer, Mekanism.rl(basePath + gas.getName()));
    }
}