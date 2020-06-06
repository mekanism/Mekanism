package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Items;

class GasConversionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "gas_conversion/";
        //Flint -> oxygen
        ItemStackToChemicalRecipeBuilder.gasConversion(
              ItemStackIngredient.from(Items.FLINT),
              MekanismGases.OXYGEN.getStack(10)
        ).build(consumer, Mekanism.rl(basePath + "flint_to_oxygen"));
        //Osmium block -> osmium
        ItemStackToChemicalRecipeBuilder.gasConversion(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.OSMIUM)),
              MekanismGases.LIQUID_OSMIUM.getStack(1_800)
        ).build(consumer, Mekanism.rl(basePath + "osmium_from_block"));
        //Osmium ingot -> osmium
        ItemStackToChemicalRecipeBuilder.gasConversion(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)),
              MekanismGases.LIQUID_OSMIUM.getStack(200)
        ).build(consumer, Mekanism.rl(basePath + "osmium_from_ingot"));
        //Salt -> hydrogen chloride
        ItemStackToChemicalRecipeBuilder.gasConversion(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SALT),
              MekanismGases.HYDROGEN_CHLORIDE.getStack(2)
        ).build(consumer, Mekanism.rl(basePath + "salt_to_hydrogen_chloride"));
        //Sulfur -> sulfuric acid
        ItemStackToChemicalRecipeBuilder.gasConversion(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SULFUR),
              MekanismGases.SULFURIC_ACID.getStack(2)
        ).build(consumer, Mekanism.rl(basePath + "sulfur_to_sulfuric_acid"));
    }
}