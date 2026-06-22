package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.ItemIds;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class GasConversionRecipeProvider extends BaseSubRecipeProvider {

    GasConversionRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "chemical_conversion/";
        //Flint -> oxygen
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(this.items, ItemIds.FLINT),
              chemicalTemplate(MekanismChemicals.OXYGEN, 10)
        ).save(consumer, Mekanism.rl(basePath + "flint_to_oxygen"));
        //Osmium block -> osmium
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.BlockItems.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.OSMIUM)),
              chemicalTemplate(MekanismChemicals.OSMIUM, 1_800)
        ).save(consumer, Mekanism.rl(basePath + "osmium_from_block"));
        //Osmium ingot -> osmium
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM)),
              chemicalTemplate(MekanismChemicals.OSMIUM, 200)
        ).save(consumer, Mekanism.rl(basePath + "osmium_from_ingot"));
        //Salt -> hydrogen chloride
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_SALT),
              chemicalTemplate(MekanismChemicals.HYDROGEN_CHLORIDE, 2)
        ).save(consumer, Mekanism.rl(basePath + "salt_to_hydrogen_chloride"));
        //Sulfur -> sulfuric acid
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_SULFUR),
              chemicalTemplate(MekanismChemicals.SULFURIC_ACID, 2)
        ).save(consumer, Mekanism.rl(basePath + "sulfur_to_sulfuric_acid"));
    }
}