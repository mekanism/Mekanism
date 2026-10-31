package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class GasConversionRecipeProvider extends BaseSubRecipeProvider {

    GasConversionRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "chemical_conversion/";
        //Flint -> oxygen
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(this.items, ItemIds.FLINT),
              chemicalTemplate(ChemicalIds.OXYGEN, 10)
        ).unlockedBy(hasCriterion(ItemIds.FLINT))
              .save(output, basePath + "flint_to_oxygen");
        //Osmium block -> osmium
        TagKey<Item> osmiumBlock = MekanismTags.BlockItems.PROCESSED_RESOURCE_BLOCKS.get(BlockResourceInfo.OSMIUM).item();
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.BlockItems.PROCESSED_RESOURCE_BLOCKS.get(BlockResourceInfo.OSMIUM)),
              chemicalTemplate(ChemicalIds.OSMIUM, 1_800)
        ).group(ChemicalIds.OSMIUM)
              .unlockedBy("has_osmium", has(osmiumBlock))
              .save(output, basePath + "osmium_from_block");
        //Osmium ingot -> osmium
        TagKey<Item> osmiumIngot = MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM);
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(this.items, osmiumIngot),
              chemicalTemplate(ChemicalIds.OSMIUM, 200)
        ).group(ChemicalIds.OSMIUM)
              .unlockedBy("has_osmium", has(osmiumIngot))
              .save(output, basePath + "osmium_from_ingot");
        //Salt -> hydrogen chloride
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_SALT),
              chemicalTemplate(ChemicalIds.HYDROGEN_CHLORIDE, 2)
        ).unlockedBy("has_salt", has(MekanismTags.Items.DUSTS_SALT))
              .save(output, basePath + "salt_to_hydrogen_chloride");
        //Sulfur -> sulfuric acid
        ItemStackToChemicalRecipeBuilder.chemicalConversion(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_SULFUR),
              chemicalTemplate(ChemicalIds.SULFURIC_ACID, 2)
        ).unlockedBy("has_sulfur_dusut", has(MekanismTags.Items.DUSTS_SULFUR))
              .save(output, basePath + "sulfur_to_sulfuric_acid");
    }
}