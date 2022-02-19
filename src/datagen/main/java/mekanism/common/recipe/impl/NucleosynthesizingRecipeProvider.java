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
              ItemStackIngredient.from(Items.SKELETON_SKULL),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 5),
              new ItemStack(Items.WITHER_SKELETON_SKULL),
              1_250
        ).build(consumer, Mekanism.rl(basePath + "wither_skeleton_skull"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Tags.Items.NETHER_STARS),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 5),
              new ItemStack(Items.HEART_OF_THE_SEA),
              1_250
        ).build(consumer, Mekanism.rl(basePath + "heart_of_the_sea"));

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
        //Note: This is intentionally a diamond sword and not a netherite sword, as the diamond sword
        // is a lot closer in color scheme to a trident, so it makes a bit more sense
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.DIAMOND_SWORD),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 4),
              new ItemStack(Items.TRIDENT),
              1_000
        ).build(consumer, Mekanism.rl(basePath + "trident"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Blocks.BEACON),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Items.END_CRYSTAL),
              750
        ).build(consumer, Mekanism.rl(basePath + "end_crystal"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(ItemTags.BEDS),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Blocks.RESPAWN_ANCHOR),
              750
        ).build(consumer, Mekanism.rl(basePath + "respawn_anchor"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.FLOWER_POT),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Items.NAUTILUS_SHELL),
              750
        ).build(consumer, Mekanism.rl(basePath + "nautilus_shell"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.GOLDEN_APPLE),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Items.ENCHANTED_GOLDEN_APPLE),
              750
        ).build(consumer, Mekanism.rl(basePath + "enchanted_golden_apple"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Tags.Items.LEATHER),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Items.PHANTOM_MEMBRANE),
              750
        ).build(consumer, Mekanism.rl(basePath + "phantom_membrane"));

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
              ItemStackIngredient.from(Tags.Items.CHESTS_WOODEN),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.ENDER_CHEST),
              500
        ).build(consumer, Mekanism.rl(basePath + "ender_chest"));

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
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              ItemStackIngredient.from(Items.BOW),
              GasStackIngredient.from(MekanismGases.ANTIMATTER, 1),
              new ItemStack(Items.CROSSBOW),
              200
        ).build(consumer, Mekanism.rl(basePath + "crossbow"));
    }
}