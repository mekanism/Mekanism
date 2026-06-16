package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.NucleosynthesizingRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class NucleosynthesizingRecipeProvider extends BaseSubRecipeProvider {

    NucleosynthesizingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "nucleosynthesizing/";
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SKELETON_SKULL),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 5),
              template(BlockItemIds.WITHER_SKELETON_SKULL),
              1_250,
              false
        ).save(consumer, Mekanism.rl(basePath + "wither_skeleton_skull"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.NETHER_STARS),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 5),
              template(ItemIds.HEART_OF_THE_SEA),
              1_250,
              false
        ).save(consumer, Mekanism.rl(basePath + "heart_of_the_sea"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.COAL),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 4),
              template(ItemIds.DIAMOND),
              1_000,
              false
        ).save(consumer, Mekanism.rl(basePath + "diamond"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.DIAMOND),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 4),
              template(ItemIds.EMERALD),
              1_000,
              false
        ).save(consumer, Mekanism.rl(basePath + "emerald"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.EGG),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 4),
              template(BlockItemIds.DRAGON_EGG),
              1_000,
              false
        ).save(consumer, Mekanism.rl(basePath + "dragon_egg"));
        //Note: This is intentionally a diamond sword and not a netherite sword, as the diamond sword
        // is a lot closer in color scheme to a trident, so it makes a bit more sense
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.DIAMOND_SWORD),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 4),
              template(ItemIds.TRIDENT),
              1_000,
              false
        ).save(consumer, Mekanism.rl(basePath + "trident"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.BEACON),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 3),
              template(ItemIds.END_CRYSTAL),
              750,
              false
        ).save(consumer, Mekanism.rl(basePath + "end_crystal"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.BEDS),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 3),
              template(BlockItemIds.RESPAWN_ANCHOR),
              750,
              false
        ).save(consumer, Mekanism.rl(basePath + "respawn_anchor"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.FLOWER_POT),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 3),
              template(ItemIds.NAUTILUS_SHELL),
              750,
              false
        ).save(consumer, Mekanism.rl(basePath + "nautilus_shell"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.GOLDEN_APPLE),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 3),
              template(ItemIds.ENCHANTED_GOLDEN_APPLE),
              750,
              false
        ).save(consumer, Mekanism.rl(basePath + "enchanted_golden_apple"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.LEATHERS),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 3),
              template(ItemIds.PHANTOM_MEMBRANE),
              750,
              false
        ).save(consumer, Mekanism.rl(basePath + "phantom_membrane"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.WOOL.red()),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              template(BlockItemIds.REDSTONE_BLOCK),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "redstone_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.WOOL.yellow()),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              template(BlockItemIds.GLOWSTONE),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "glowstone_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.WOOL.blue()),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              template(BlockItemIds.LAPIS_BLOCK),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "lapis_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.WOOL.lightGray()),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              template(BlockItemIds.QUARTZ_BLOCK),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "quartz_block"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemTags.SMALL_FLOWERS),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              template(BlockItemIds.CHORUS_FLOWER),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "chorus_flower"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.CHESTS_WOODEN),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              template(BlockItemIds.ENDER_CHEST),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "ender_chest"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GEMS_AMETHYST),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 2),
              template(ItemIds.ECHO_SHARD),
              500,
              false
        ).save(consumer, Mekanism.rl(basePath + "echo_shard"));

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.TIN)),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 1),
              template(ItemIds.IRON_INGOT),
              200,
              false
        ).save(consumer, Mekanism.rl(basePath + "iron"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.OBSIDIANS_NORMAL),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 1),
              template(BlockItemIds.CRYING_OBSIDIAN),
              200,
              false
        ).save(consumer, Mekanism.rl(basePath + "crying_obsidian"));
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.BOW),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 1),
              template(ItemIds.CROSSBOW),
              200,
              false
        ).save(consumer, Mekanism.rl(basePath + "crossbow"));
    }
}