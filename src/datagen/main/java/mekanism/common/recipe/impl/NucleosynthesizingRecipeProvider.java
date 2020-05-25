package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.NucleosynthesizingRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

class NucleosynthesizingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "nucleosynthesizing/";
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.COAL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 4),
              new ItemStack(Items.DIAMOND),
              1000
        ).build(consumer, Mekanism.rl(basePath + "diamond"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.DIAMOND),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 4),
              new ItemStack(Items.EMERALD),
              1000
        ).build(consumer, Mekanism.rl(basePath + "emerald"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.RED_WOOL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Items.REDSTONE_BLOCK),
              500
        ).build(consumer, Mekanism.rl(basePath + "redstone_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.YELLOW_WOOL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Items.GLOWSTONE),
              500
        ).build(consumer, Mekanism.rl(basePath + "glowstone_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.BLUE_WOOL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Items.LAPIS_BLOCK),
              500
        ).build(consumer, Mekanism.rl(basePath + "lapis_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.LIGHT_GRAY_WOOL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Items.QUARTZ_BLOCK),
              500
        ).build(consumer, Mekanism.rl(basePath + "quartz_block"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 1),
              new ItemStack(Items.IRON_INGOT),
              200
        ).build(consumer, Mekanism.rl(basePath + "iron"));
    }
}