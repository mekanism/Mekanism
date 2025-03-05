package mekanism.common.recipe.impl;

import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

class ControlCircuitRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern circuitPattern = RecipePattern.createPattern(TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY));

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "control_circuit/";
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.REDSTONE, 20),
              MekanismItems.BASIC_CONTROL_CIRCUIT.asStack(),
              false
        ).build(consumer, Mekanism.rl(basePath + "basic"));
        addCircuitUpgradeRecipe(consumer, MekanismItems.ADVANCED_CONTROL_CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC, MekanismTags.Items.ALLOYS_INFUSED, basePath, "advanced");
        addCircuitUpgradeRecipe(consumer, MekanismItems.ELITE_CONTROL_CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED, MekanismTags.Items.ALLOYS_REINFORCED, basePath, "elite");
        addCircuitUpgradeRecipe(consumer, MekanismItems.ULTIMATE_CONTROL_CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE, MekanismTags.Items.ALLOYS_ATOMIC, basePath, "ultimate");

        //infusion variants that save on the base ingots needed, but take extra infusion material
        addCircuitInfusionUpgrade(consumer, MekanismItems.ADVANCED_CONTROL_CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC, MekanismAPITags.Chemicals.REDSTONE, 10, basePath, "advanced");
        addCircuitInfusionUpgrade(consumer, MekanismItems.ELITE_CONTROL_CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED, MekanismAPITags.Chemicals.DIAMOND, 20, basePath, "elite");
        addCircuitInfusionUpgrade(consumer, MekanismItems.ULTIMATE_CONTROL_CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE, MekanismAPITags.Chemicals.REFINED_OBSIDIAN, 40, basePath, "ultimate");
    }

    private void addCircuitUpgradeRecipe(RecipeOutput consumer, Holder<Item> output, TagKey<Item> circuitTag, TagKey<Item> alloyTag, String basePath,
          String name) {
        ExtendedShapedRecipeBuilder.shapedRecipe(output)
              .pattern(circuitPattern)
              .key(Pattern.CIRCUIT, circuitTag)
              .key(Pattern.ALLOY, alloyTag)
              .build(consumer, Mekanism.rl(basePath + name));
    }

    private void addCircuitInfusionUpgrade(RecipeOutput consumer, Holder<Item> output, TagKey<Item> circuitTag, TagKey<Chemical> infusionType, int singleAlloyAmount, String basePath, String name) {
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(circuitTag),
              IngredientCreatorAccess.chemicalStack().from(infusionType, singleAlloyAmount * 6), /* 3x 2 alloys */
              new ItemStack(output),
              false
        ).build(consumer, Mekanism.rl(basePath + "infused_" + name));
    }
}