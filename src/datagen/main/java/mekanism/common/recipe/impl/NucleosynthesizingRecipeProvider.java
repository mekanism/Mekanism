package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.NucleosynthesizingRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

class NucleosynthesizingRecipeProvider implements ISubRecipeProvider {

    private final HolderGetter<Item> items;

    public NucleosynthesizingRecipeProvider(HolderGetter<Item> items) {
        this.items = items;
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "nucleosynthesizing/";
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.SKELETON_SKULL),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 5),
              new ItemStackTemplate(Items.WITHER_SKELETON_SKULL),
              1_250,
              false
        ).save(consumer, Mekanism.rl(basePath + "wither_skeleton_skull"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.NETHER_STARS),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 5),
              new ItemStackTemplate(Items.HEART_OF_THE_SEA),
              1_250,
              false
        ).save(consumer, Mekanism.rl(basePath + "heart_of_the_sea"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.COAL),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 4),
              new ItemStackTemplate(Items.DIAMOND),
              1_000,
              false
        ).save(consumer, Mekanism.rl(basePath + "diamond"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.DIAMOND),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 4),
              new ItemStackTemplate(Items.EMERALD),
              1_000,
              false
        ).save(consumer, Mekanism.rl(basePath + "emerald"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.EGG),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 4),
              new ItemStackTemplate(Items.DRAGON_EGG),
              1_000,
              false
        ).save(consumer, Mekanism.rl(basePath + "dragon_egg"));
        //Note: This is intentionally a diamond sword and not a netherite sword, as the diamond sword
        // is a lot closer in color scheme to a trident, so it makes a bit more sense
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.DIAMOND_SWORD),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 4),
              new ItemStackTemplate(Items.TRIDENT),
              1_000,
              false
        ).save(consumer, Mekanism.rl(basePath + "trident"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.BEACON),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 3),
              new ItemStackTemplate(Items.END_CRYSTAL),
              750,
              false
        ).save(consumer, Mekanism.rl(basePath + "end_crystal"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.BEDS),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 3),
              new ItemStackTemplate(Items.RESPAWN_ANCHOR),
              750,
              false
        ).save(consumer, Mekanism.rl(basePath + "respawn_anchor"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.FLOWER_POT),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 3),
              new ItemStackTemplate(Items.NAUTILUS_SHELL),
              750,
              false
        ).save(consumer, Mekanism.rl(basePath + "nautilus_shell"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.GOLDEN_APPLE),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 3),
              new ItemStackTemplate(Items.ENCHANTED_GOLDEN_APPLE),
              750,
              false
        ).save(consumer, Mekanism.rl(basePath + "enchanted_golden_apple"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.LEATHERS),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 3),
              new ItemStackTemplate(Items.PHANTOM_MEMBRANE),
              750,
              false
        ).save(consumer, Mekanism.rl(basePath + "phantom_membrane"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.WOOL.red()),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              new ItemStackTemplate(Items.REDSTONE_BLOCK),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "redstone_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.WOOL.yellow()),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              new ItemStackTemplate(Items.GLOWSTONE),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "glowstone_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.WOOL.blue()),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              new ItemStackTemplate(Items.LAPIS_BLOCK),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "lapis_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.WOOL.lightGray()),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              new ItemStackTemplate(Items.QUARTZ_BLOCK),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "quartz_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemTags.SMALL_FLOWERS.item()),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              new ItemStackTemplate(Items.CHORUS_FLOWER),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "chorus_flower"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.CHESTS_WOODEN),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              new ItemStackTemplate(Items.ENDER_CHEST),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "ender_chest"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GEMS_AMETHYST),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              new ItemStackTemplate(Items.ECHO_SHARD),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "echo_shard"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.TIN)),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 1),
              new ItemStackTemplate(Items.IRON_INGOT),
              200,
              false
        ).save(consumer, Mekanism.rl(basePath + "iron"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.OBSIDIANS_NORMAL),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 1),
              new ItemStackTemplate(Items.CRYING_OBSIDIAN),
              200,
              false
        ).save(consumer, Mekanism.rl(basePath + "crying_obsidian"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(Items.BOW),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 1),
              new ItemStackTemplate(Items.CROSSBOW),
              200,
              false
        ).save(consumer, Mekanism.rl(basePath + "crossbow"));
    }
}