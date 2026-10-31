package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.NucleosynthesizingRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class NucleosynthesizingRecipeProvider extends BaseSubRecipeProvider {

    NucleosynthesizingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "nucleosynthesizing/";
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SKELETON_SKULL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 5),
              false,
              template(BlockItemIds.WITHER_SKELETON_SKULL),
              1_250
        ).unlockedBy("has_skull", has(BlockItemIds.SKELETON_SKULL))
              .save(output, basePath + "wither_skeleton_skull");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.NETHER_STARS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 5),
              false,
              template(ItemIds.HEART_OF_THE_SEA),
              1_250
        ).unlockedBy("has_star", has(Tags.Items.NETHER_STARS))
              .save(output, basePath + "heart_of_the_sea");

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.COAL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 4),
              false,
              template(ItemIds.DIAMOND),
              1_000
        ).unlockedBy(hasCriterion(ItemIds.COAL))
              .save(output, basePath + "diamond");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GEMS_DIAMOND),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 4),
              false,
              template(ItemIds.EMERALD),
              1_000
        ).unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
              .save(output, basePath + "emerald");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.EGGS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 4),
              false,
              template(BlockItemIds.DRAGON_EGG),
              1_000
        ).unlockedBy("has_egg", has(ItemTags.EGGS))
              .save(output, basePath + "dragon_egg");
        //Note: This is intentionally a diamond sword and not a netherite sword, as the diamond sword
        // is a lot closer in color scheme to a trident, so it makes a bit more sense
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.DIAMOND_SWORD),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 4),
              false,
              template(ItemIds.TRIDENT),
              1_000
        ).unlockedBy("has_sword", has(ItemIds.DIAMOND_SWORD))
              .save(output, basePath + "trident");

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.BEACON),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 3),
              false,
              template(ItemIds.END_CRYSTAL),
              750
        ).unlockedBy(hasCriterion(BlockItemIds.BEACON))
              .save(output, basePath + "end_crystal");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.BEDS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 3),
              false,
              template(BlockItemIds.RESPAWN_ANCHOR),
              750
        ).unlockedBy("has_bed", has(ItemTags.BEDS))
              .save(output, basePath + "respawn_anchor");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.FLOWER_POT),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 3),
              false,
              template(ItemIds.NAUTILUS_SHELL),
              750
        ).unlockedBy(hasCriterion(BlockItemIds.FLOWER_POT))
              .save(output, basePath + "nautilus_shell");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.GOLDEN_APPLE),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 3),
              false,
              template(ItemIds.ENCHANTED_GOLDEN_APPLE),
              750
        ).unlockedBy("has_apple", has(ItemIds.GOLDEN_APPLE))
              .save(output, basePath + "enchanted_golden_apple");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.LEATHERS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 3),
              false,
              template(ItemIds.PHANTOM_MEMBRANE),
              750
        ).unlockedBy("has_leather", has(Tags.Items.LEATHERS))
              .save(output, basePath + "phantom_membrane");

        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.WOOL.red()),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 2),
              false,
              template(BlockItemIds.REDSTONE_BLOCK),
              500
        ).unlockedBy("has_wool", has(ItemTags.WOOL))
              .save(output, basePath + "redstone_block");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.WOOL.yellow()),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 2),
              false,
              template(BlockItemIds.GLOWSTONE),
              500
        ).unlockedBy("has_wool", has(ItemTags.WOOL))
              .save(output, basePath + "glowstone_block");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.WOOL.blue()),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 2),
              false,
              template(BlockItemIds.LAPIS_BLOCK),
              500
        ).unlockedBy("has_wool", has(ItemTags.WOOL))
              .save(output, basePath + "lapis_block");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.WOOL.lightGray()),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 2),
              false,
              template(BlockItemIds.QUARTZ_BLOCK),
              500
        ).unlockedBy("has_wool", has(ItemTags.WOOL))
              .save(output, basePath + "quartz_block");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.WOOL_SLAB.lightGray()),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 1),
              false,
              template(BlockItemIds.QUARTZ_SLAB),
              250
        ).unlockedBy("has_wool", has(ItemTags.WOOL_SLABS))
              .save(output, basePath + "quartz_slab");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.WOOL_STAIRS.lightGray()),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 2),
              false,
              template(BlockItemIds.QUARTZ_STAIRS),
              500
        ).unlockedBy("has_wool", has(ItemTags.WOOL_STAIRS))
              .save(output, basePath + "quartz_stairs");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.FLOWERS_SMALL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 2),
              false,
              template(BlockItemIds.CHORUS_FLOWER),
              500
        ).unlockedBy("has_flower", has(Tags.BlockItems.FLOWERS_SMALL))
              .save(output, basePath + "chorus_flower");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.CHESTS_WOODEN),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 2),
              false,
              template(BlockItemIds.ENDER_CHEST),
              500
        ).unlockedBy("has_chest", has(Tags.BlockItems.CHESTS_WOODEN))
              .save(output, basePath + "ender_chest");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GEMS_AMETHYST),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 2),
              false,
              template(ItemIds.ECHO_SHARD),
              500
        ).unlockedBy("has_amethyst", has(Tags.Items.GEMS_AMETHYST))
              .save(output, basePath + "echo_shard");

        TagKey<Item> tinIngot = MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.TIN);
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, tinIngot),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 1),
              false,
              template(ItemIds.IRON_INGOT),
              200
        ).unlockedBy("has_tin_ingot", has(tinIngot))
              .save(output, basePath + "iron");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.OBSIDIANS_NORMAL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 1),
              false,
              template(BlockItemIds.CRYING_OBSIDIAN),
              200
        ).unlockedBy("has_obsidian", has(Tags.BlockItems.OBSIDIANS_NORMAL))
              .save(output, basePath + "crying_obsidian");
        NucleosynthesizingRecipeBuilder.nucleosynthesizing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.BOW),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 1),
              false,
              template(ItemIds.CROSSBOW),
              200
        ).unlockedBy(hasCriterion(ItemIds.BOW))
              .save(output, basePath + "crossbow");
    }
}