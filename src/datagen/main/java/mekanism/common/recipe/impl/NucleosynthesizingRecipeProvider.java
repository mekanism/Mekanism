package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.NucleosynthesizingRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

class NucleosynthesizingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "nucleosynthesizing/";
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.SKELETON_SKULL),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 5),
              new ItemStack(Items.WITHER_SKELETON_SKULL),
              1_250
        ).build(consumer, Mekanism.rl(basePath + "wither_skeleton_skull"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Tags.Items.NETHER_STARS),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 5),
              new ItemStack(Items.HEART_OF_THE_SEA),
              1_250
        ).build(consumer, Mekanism.rl(basePath + "heart_of_the_sea"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.COAL),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 4),
              new ItemStack(Items.DIAMOND),
              1_000
        ).build(consumer, Mekanism.rl(basePath + "diamond"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.DIAMOND),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 4),
              new ItemStack(Items.EMERALD),
              1_000
        ).build(consumer, Mekanism.rl(basePath + "emerald"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.EGG),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 4),
              new ItemStack(Items.DRAGON_EGG),
              1_000
        ).build(consumer, Mekanism.rl(basePath + "dragon_egg"));
        //Note: This is intentionally a diamond sword and not a netherite sword, as the diamond sword
        // is a lot closer in color scheme to a trident, so it makes a bit more sense
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.DIAMOND_SWORD),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 4),
              new ItemStack(Items.TRIDENT),
              1_000
        ).build(consumer, Mekanism.rl(basePath + "trident"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Blocks.BEACON),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Items.END_CRYSTAL),
              750
        ).build(consumer, Mekanism.rl(basePath + "end_crystal"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(ItemTags.BEDS),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Blocks.RESPAWN_ANCHOR),
              750
        ).build(consumer, Mekanism.rl(basePath + "respawn_anchor"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.FLOWER_POT),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Items.NAUTILUS_SHELL),
              750
        ).build(consumer, Mekanism.rl(basePath + "nautilus_shell"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.GOLDEN_APPLE),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Items.ENCHANTED_GOLDEN_APPLE),
              750
        ).build(consumer, Mekanism.rl(basePath + "enchanted_golden_apple"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Tags.Items.LEATHER),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 3),
              new ItemStack(Items.PHANTOM_MEMBRANE),
              750
        ).build(consumer, Mekanism.rl(basePath + "phantom_membrane"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Blocks.RED_WOOL),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.REDSTONE_BLOCK),
              500
        ).build(consumer, Mekanism.rl(basePath + "redstone_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Blocks.YELLOW_WOOL),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.GLOWSTONE),
              500
        ).build(consumer, Mekanism.rl(basePath + "glowstone_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Blocks.BLUE_WOOL),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.LAPIS_BLOCK),
              500
        ).build(consumer, Mekanism.rl(basePath + "lapis_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Blocks.LIGHT_GRAY_WOOL),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.QUARTZ_BLOCK),
              500
        ).build(consumer, Mekanism.rl(basePath + "quartz_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(ItemTags.SMALL_FLOWERS),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.CHORUS_FLOWER),
              500
        ).build(consumer, Mekanism.rl(basePath + "chorus_flower"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Tags.Items.CHESTS_WOODEN),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 2),
              new ItemStack(Blocks.ENDER_CHEST),
              500
        ).build(consumer, Mekanism.rl(basePath + "ender_chest"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 1),
              new ItemStack(Items.IRON_INGOT),
              200
        ).build(consumer, Mekanism.rl(basePath + "iron"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Tags.Items.OBSIDIAN),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 1),
              new ItemStack(Blocks.CRYING_OBSIDIAN),
              200
        ).build(consumer, Mekanism.rl(basePath + "crying_obsidian"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.BOW),
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 1),
              new ItemStack(Items.CROSSBOW),
              200
        ).build(consumer, Mekanism.rl(basePath + "crossbow"));
    }
}