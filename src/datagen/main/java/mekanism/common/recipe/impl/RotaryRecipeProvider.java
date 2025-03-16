package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.RotaryRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registration.impl.DeferredChemical;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

class RotaryRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "rotary/";
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.BRINE, MekanismFluids.BRINE, MekanismTags.Fluids.BRINE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.CHLORINE, MekanismFluids.CHLORINE, MekanismTags.Fluids.CHLORINE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.ETHENE, MekanismFluids.ETHENE, MekanismTags.Fluids.ETHENE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.HYDROGEN, MekanismFluids.HYDROGEN, MekanismTags.Fluids.HYDROGEN);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.HYDROGEN_CHLORIDE, MekanismFluids.HYDROGEN_CHLORIDE, MekanismTags.Fluids.HYDROGEN_CHLORIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.LITHIUM, MekanismFluids.LITHIUM, MekanismTags.Fluids.LITHIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.OXYGEN, MekanismFluids.OXYGEN, MekanismTags.Fluids.OXYGEN);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.SODIUM, MekanismFluids.SODIUM, MekanismTags.Fluids.SODIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.SUPERHEATED_SODIUM, MekanismFluids.SUPERHEATED_SODIUM, MekanismTags.Fluids.SUPERHEATED_SODIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.STEAM, MekanismFluids.STEAM, MekanismTags.Fluids.STEAM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.SULFUR_DIOXIDE, MekanismFluids.SULFUR_DIOXIDE, MekanismTags.Fluids.SULFUR_DIOXIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.SULFUR_TRIOXIDE, MekanismFluids.SULFUR_TRIOXIDE, MekanismTags.Fluids.SULFUR_TRIOXIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.SULFURIC_ACID, MekanismFluids.SULFURIC_ACID, MekanismTags.Fluids.SULFURIC_ACID);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.HYDROFLUORIC_ACID, MekanismFluids.HYDROFLUORIC_ACID, MekanismTags.Fluids.HYDROFLUORIC_ACID);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.URANIUM_OXIDE, MekanismFluids.URANIUM_OXIDE, MekanismTags.Fluids.URANIUM_OXIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.URANIUM_HEXAFLUORIDE, MekanismFluids.URANIUM_HEXAFLUORIDE, MekanismTags.Fluids.URANIUM_HEXAFLUORIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismChemicals.WATER_VAPOR, Fluids.WATER.builtInRegistryHolder(), FluidTags.WATER);
    }

    private void addRotaryCondensentratorRecipe(RecipeOutput consumer, String basePath, DeferredChemical<Chemical> gas, Holder<Fluid> fluidOutput, TagKey<Fluid> fluidInput) {
        RotaryRecipeBuilder.rotary(
              IngredientCreatorAccess.fluid().from(fluidInput, 1),
              IngredientCreatorAccess.chemicalStack().fromHolder(gas, 1),
              gas.asStack(1),
              new FluidStack(fluidOutput, 1)
        ).build(consumer, Mekanism.rl(basePath + gas.getName()));
    }
}