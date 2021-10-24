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
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

class NucleosynthesizingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "nucleosynthesizing/";
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.COAL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 4),
              new ItemStack(Items.DIAMOND),
              1_000
        ).build(consumer, Mekanism.rl(basePath + "diamond"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.DIAMOND),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 4),
              new ItemStack(Items.EMERALD),
              1_000
        ).build(consumer, Mekanism.rl(basePath + "emerald"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.EGG),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 4),
              new ItemStack(Items.DRAGON_EGG),
              1_000
        ).build(consumer, Mekanism.rl(basePath + "dragon_egg"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Blocks.BEACON),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Items.END_CRYSTAL),
              750
        ).build(consumer, Mekanism.rl(basePath + "end_crystal"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Blocks.RED_WOOL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.REDSTONE_BLOCK),
              500
        ).build(consumer, Mekanism.rl(basePath + "redstone_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Blocks.YELLOW_WOOL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.GLOWSTONE),
              500
        ).build(consumer, Mekanism.rl(basePath + "glowstone_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Blocks.BLUE_WOOL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.LAPIS_BLOCK),
              500
        ).build(consumer, Mekanism.rl(basePath + "lapis_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Blocks.LIGHT_GRAY_WOOL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.QUARTZ_BLOCK),
              500
        ).build(consumer, Mekanism.rl(basePath + "quartz_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(ItemTags.SMALL_FLOWERS),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.CHORUS_FLOWER),
              500
        ).build(consumer, Mekanism.rl(basePath + "chorus_flower"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 1),
              new ItemStack(Items.IRON_INGOT),
              200
        ).build(consumer, Mekanism.rl(basePath + "iron"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Tags.Items.OBSIDIAN),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 1),
              new ItemStack(Blocks.CRYING_OBSIDIAN),
              200
        ).build(consumer, Mekanism.rl(basePath + "crying_obsidian"));
    }
}