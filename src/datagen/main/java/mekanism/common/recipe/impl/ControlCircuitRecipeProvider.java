package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.MetallurgicInfuserRecipeBuilder;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
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
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;

class ControlCircuitRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern circuitPattern = RecipePattern.createPattern(TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY));

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "control_circuit/";
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.REDSTONE, 20),
              MekanismItems.BASIC_CONTROL_CIRCUIT.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "basic"));
        addCircuitUpgradeRecipe(consumer, MekanismItems.ADVANCED_CONTROL_CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC, MekanismTags.Items.ALLOYS_INFUSED, basePath, "advanced");
        addCircuitUpgradeRecipe(consumer, MekanismItems.ELITE_CONTROL_CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED, MekanismTags.Items.ALLOYS_REINFORCED, basePath, "elite");
        addCircuitUpgradeRecipe(consumer, MekanismItems.ULTIMATE_CONTROL_CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE, MekanismTags.Items.ALLOYS_ATOMIC, basePath, "ultimate");
    }

    private void addCircuitUpgradeRecipe(Consumer<IFinishedRecipe> consumer, IItemProvider output, ITag<Item> circuitTag, ITag<Item> alloyTag, String basePath, String name) {
        ExtendedShapedRecipeBuilder.shapedRecipe(output)
              .pattern(circuitPattern)
              .key(Pattern.CIRCUIT, circuitTag)
              .key(Pattern.ALLOY, alloyTag)
              .build(consumer, Mekanism.rl(basePath + name));
    }
}