package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

class GasConversionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "chemical_conversion/";
        //Flint -> oxygen
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(Items.FLINT),
              MekanismChemicals.OXYGEN.asStack(10)
        ).build(consumer, Mekanism.rl(basePath + "flint_to_oxygen"));
        //Osmium block -> osmium
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.OSMIUM)),
              MekanismChemicals.OSMIUM.asStack(1_800)
        ).build(consumer, Mekanism.rl(basePath + "osmium_from_block"));
        //Osmium ingot -> osmium
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)),
              MekanismChemicals.OSMIUM.asStack(200)
        ).build(consumer, Mekanism.rl(basePath + "osmium_from_ingot"));
        //Salt -> hydrogen chloride
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_SALT),
              MekanismChemicals.HYDROGEN_CHLORIDE.asStack(2)
        ).build(consumer, Mekanism.rl(basePath + "salt_to_hydrogen_chloride"));
        //Sulfur -> sulfuric acid
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_SULFUR),
              MekanismChemicals.SULFURIC_ACID.asStack(2)
        ).build(consumer, Mekanism.rl(basePath + "sulfur_to_sulfuric_acid"));
    }
}