package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.builder.RotaryRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidIds;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

class RotaryRecipeProvider extends BaseSubRecipeProvider {

    RotaryRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "rotary/";
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.BRINE, MekanismFluids.BRINE, MekanismTags.Fluids.BRINE);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.CHLORINE, MekanismFluids.CHLORINE, MekanismTags.Fluids.CHLORINE);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.ETHENE, MekanismFluids.ETHENE, MekanismTags.Fluids.ETHENE);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.HYDROGEN, MekanismFluids.HYDROGEN, MekanismTags.Fluids.HYDROGEN);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.HYDROGEN_CHLORIDE, MekanismFluids.HYDROGEN_CHLORIDE, MekanismTags.Fluids.HYDROGEN_CHLORIDE);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.LITHIUM, MekanismFluids.LITHIUM, MekanismTags.Fluids.LITHIUM);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.OXYGEN, MekanismFluids.OXYGEN, MekanismTags.Fluids.OXYGEN);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.SODIUM, MekanismFluids.SODIUM, MekanismTags.Fluids.SODIUM);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.SUPERHEATED_SODIUM, MekanismFluids.SUPERHEATED_SODIUM, MekanismTags.Fluids.SUPERHEATED_SODIUM);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.STEAM, MekanismFluids.STEAM, MekanismTags.Fluids.STEAM);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.SULFUR_DIOXIDE, MekanismFluids.SULFUR_DIOXIDE, MekanismTags.Fluids.SULFUR_DIOXIDE);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.SULFUR_TRIOXIDE, MekanismFluids.SULFUR_TRIOXIDE, MekanismTags.Fluids.SULFUR_TRIOXIDE);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.SULFURIC_ACID, MekanismFluids.SULFURIC_ACID, MekanismTags.Fluids.SULFURIC_ACID);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.HYDROFLUORIC_ACID, MekanismFluids.HYDROFLUORIC_ACID, MekanismTags.Fluids.HYDROFLUORIC_ACID);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.URANIUM_OXIDE, MekanismFluids.URANIUM_OXIDE, MekanismTags.Fluids.URANIUM_OXIDE);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.URANIUM_HEXAFLUORIDE, MekanismFluids.URANIUM_HEXAFLUORIDE, MekanismTags.Fluids.URANIUM_HEXAFLUORIDE);
        addRotaryCondensentratorRecipe(output, basePath, ChemicalIds.WATER_VAPOR, fluids.getOrThrow(FluidIds.WATER), FluidTags.WATER);
    }

    private void addRotaryCondensentratorRecipe(RecipeOutput output, String basePath, ResourceKey<Chemical> chemical, Holder<Fluid> fluidOutput, TagKey<Fluid> fluidInput) {
        Holder<Chemical> chemicalHolder = chemicals.getOrThrow(chemical);
        RotaryRecipeBuilder.rotary(
              IngredientCreatorAccess.fluid().from(this.fluids, fluidInput, 1),
              IngredientCreatorAccess.chemicalStack().fromHolder(chemicalHolder, 1),
              new ChemicalStackTemplate(chemicalHolder, 1),
              new FluidStackTemplate(fluidOutput, 1)
        ).unlockedBy("has_condensentrator", has(MekanismBlocks.ROTARY_CONDENSENTRATOR))
              .save(output, basePath + chemical.identifier().getPath());
    }
}